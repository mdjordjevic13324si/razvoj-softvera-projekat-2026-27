package org.raflab.studentski_server.services;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.response.PolaganjeResponse;
import org.raflab.studentski_server.controllers.response.PredajaResponse;
import org.raflab.studentski_server.controllers.response.StanjeTermina;
import org.raflab.studentski_server.exceptions.ConflictException;
import org.raflab.studentski_server.exceptions.ForbiddenException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.repositories.GrupaRepository;
import org.raflab.studentski_server.repositories.ResenjeRepository;
import org.raflab.studentski_server.repositories.SpisakStavkaRepository;
import org.raflab.studentski_server.repositories.ZadatakRepository;
import org.springframework.stereotype.Service;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/*
 * Polaganje iz ugla studenta: da li je na spisku, kada mu je termin i preuzimanje zadatka.
 * Student se uvek uzima iz tokena, pa niko ne moze da radi u tudje ime.
 */
@AllArgsConstructor
@Service
public class PolaganjeService {

    static final DateTimeFormatter FORMAT_VREMENA = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");

    final SpisakStavkaRepository spisakStavkaRepository;
    final GrupaRepository grupaRepository;
    final ZadatakRepository zadatakRepository;
    final ResenjeRepository resenjeRepository;
    final PristupService pristupService;
    final SkladisteService skladisteService;

    // sva polaganja prijavljenog studenta (sve grupe na cijem je spisku)
    @Transactional(readOnly = true)
    public List<PolaganjeResponse> mojaPolaganja() {
        Student student = pristupService.ulogovaniStudent();
        return spisakStavkaRepository.findAllByStudentIdAndObrisanFalse(student.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    // jedno polaganje; 403 sa jasnom porukom ako student nije na spisku te grupe
    @Transactional(readOnly = true)
    public PolaganjeResponse mojePolaganje(Long grupaId) {
        return toResponse(stavkaPrijavljenogStudenta(grupaId));
    }

    /*
     * Preuzimanje zadatka. Dozvoljeno je samo ako je student na spisku grupe i samo dok termin traje.
     * Prvo preuzimanje pravi Resenje i belezi vreme; ponovno preuzimanje ne menja to vreme.
     */
    @Transactional
    public PreuzetFajl preuzmiZadatak(Long grupaId) {
        SpisakStavka stavka = stavkaPrijavljenogStudenta(grupaId);
        Termin termin = stavka.getGrupa().getTermin();
        LocalDateTime sada = LocalDateTime.now();

        if (sada.isBefore(termin.getPocetak())) {
            throw new ForbiddenException("Termin jos nije poceo. Pocetak: " + termin.getPocetak().format(FORMAT_VREMENA));
        }
        if (sada.isAfter(termin.getKraj())) {
            throw new ForbiddenException("Termin je zavrsen. Kraj: " + termin.getKraj().format(FORMAT_VREMENA));
        }
        Zadatak zadatak = zadatakGrupe(grupaId)
                .orElseThrow(() -> new ResourceNotFoundException("Zadatak za ovu grupu jos nije postavljen"));

        // par (student, zadatak) je jedinstven, pa se postojece (i obrisano) resenje koristi ponovo
        Resenje resenje = resenjeRepository.findByStudentIdAndZadatakId(stavka.getStudent().getId(), zadatak.getId())
                .orElseGet(Resenje::new);
        if (resenje.getId() == null) {
            resenje.setStudent(stavka.getStudent());
            resenje.setZadatak(zadatak);
            resenje.setVremePreuzimanja(sada);
        }
        resenje.setObrisan(false);
        resenjeRepository.save(resenje);

        return new PreuzetFajl(skladisteService.ucitaj(zadatak.getPutanjaZipa()), ZadatakService.imeFajla(zadatak));
    }

    /*
     * Predaja resenja. Dozvoljena je samo studentu sa spiska koji je preuzeo zadatak, i samo do kraja termina.
     * Student moze da preda vise puta - cuva se poslednji zip i vreme poslednje predaje.
     */
    @Transactional
    public PredajaResponse predajResenje(Long grupaId, MultipartFile fajl) {
        SpisakStavka stavka = stavkaPrijavljenogStudenta(grupaId);
        Student student = stavka.getStudent();
        Grupa grupa = stavka.getGrupa();
        Termin termin = grupa.getTermin();
        LocalDateTime sada = LocalDateTime.now();

        if (sada.isBefore(termin.getPocetak())) {
            throw new ForbiddenException("Termin jos nije poceo. Pocetak: " + termin.getPocetak().format(FORMAT_VREMENA));
        }
        if (sada.isAfter(termin.getKraj())) {
            throw new ForbiddenException("Termin je zavrsen, predaja vise nije moguca. Kraj: " + termin.getKraj().format(FORMAT_VREMENA));
        }
        Resenje resenje = resenjeStudenta(student, grupaId)
                .orElseThrow(() -> new ConflictException("Zadatak nije preuzet, pa resenje ne moze da se preda"));
        skladisteService.proveriDaJeZip(fajl);

        String putanjaZipa = putanjaResenja(grupa, student);
        skladisteService.sacuvaj(putanjaZipa, fajl);
        resenje.setPutanjaZipa(putanjaZipa);
        resenje.setVremePredaje(sada);
        resenje = resenjeRepository.save(resenje);

        return new PredajaResponse(resenje.getId(), Converters.indeks(student),
                termin.getIspit().getPredmet().getSifra(), termin.getRedniBroj(), grupa.getOznaka(),
                resenje.getVremePreuzimanja(), resenje.getVremePredaje(), fajl.getSize());
    }

    // poslednji predati zip prijavljenog studenta (npr. za nastavak rada na drugom racunaru)
    @Transactional(readOnly = true)
    public PreuzetFajl mojeResenje(Long grupaId) {
        SpisakStavka stavka = stavkaPrijavljenogStudenta(grupaId);
        Resenje resenje = resenjeStudenta(stavka.getStudent(), grupaId)
                .filter(r -> r.getPutanjaZipa() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Resenje jos nije predato"));
        return new PreuzetFajl(skladisteService.ucitaj(resenje.getPutanjaZipa()), imeFajlaResenja(stavka.getStudent()));
    }

    // resenje studenta za zadatak date grupe (nastaje pri preuzimanju zadatka)
    private Optional<Resenje> resenjeStudenta(Student student, Long grupaId) {
        return zadatakGrupe(grupaId)
                .flatMap(zadatak -> resenjeRepository.findByStudentIdAndZadatakId(student.getId(), zadatak.getId()))
                .filter(resenje -> !resenje.isObrisan());
    }

    // npr. 2026-2027/januar/RS/resenja/termin-1/grupa-A/RN-12-2023.zip
    static String putanjaResenja(Grupa grupa, Student student) {
        Termin termin = grupa.getTermin();
        Ispit ispit = termin.getIspit();
        return String.join("/",
                ZadatakService.zaPutanju(ispit.getIspitniRok().getSkolskaGodina()),
                ZadatakService.zaPutanju(ispit.getIspitniRok().getNaziv()).toLowerCase(),
                ZadatakService.zaPutanju(ispit.getPredmet().getSifra()),
                "resenja",
                "termin-" + termin.getRedniBroj(),
                "grupa-" + ZadatakService.zaPutanju(grupa.getOznaka()),
                imeFajlaResenja(student));
    }

    // npr. RN-12-2023.zip
    static String imeFajlaResenja(Student student) {
        return ZadatakService.zaPutanju(student.getSmer().getOznaka())
                + "-" + student.getBrojIndeksa() + "-" + student.getGodinaUpisa() + ".zip";
    }

    // stavka spiska za prijavljenog studenta u datoj grupi; 404 ako grupe nema, 403 ako nije na spisku
    private SpisakStavka stavkaPrijavljenogStudenta(Long grupaId) {
        Student student = pristupService.ulogovaniStudent();
        Grupa grupa = grupaRepository.findByIdAndObrisanFalse(grupaId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupa " + grupaId + " ne postoji"));
        return spisakStavkaRepository.findByStudentIdAndGrupaIdAndObrisanFalse(student.getId(), grupaId)
                .orElseThrow(() -> new ForbiddenException("Niste na spisku za polaganje: predmet "
                        + grupa.getTermin().getIspit().getPredmet().getSifra()
                        + ", termin " + grupa.getTermin().getRedniBroj()
                        + ", grupa " + grupa.getOznaka()));
    }

    private Optional<Zadatak> zadatakGrupe(Long grupaId) {
        return zadatakRepository.findByGrupaId(grupaId).filter(zadatak -> !zadatak.isObrisan());
    }

    private PolaganjeResponse toResponse(SpisakStavka stavka) {
        Grupa grupa = stavka.getGrupa();
        Termin termin = grupa.getTermin();
        Ispit ispit = termin.getIspit();
        Optional<Zadatak> zadatak = zadatakGrupe(grupa.getId());
        Optional<Resenje> resenje = zadatak
                .flatMap(z -> resenjeRepository.findByStudentIdAndZadatakId(stavka.getStudent().getId(), z.getId()))
                .filter(r -> !r.isObrisan());

        return new PolaganjeResponse(
                grupa.getId(), grupa.getOznaka(),
                termin.getId(), termin.getRedniBroj(),
                ispit.getPredmet().getSifra(), ispit.getPredmet().getNaziv(),
                ispit.getIspitniRok().getNaziv(),
                stavka.getUcionica().getOznaka(),
                termin.getPocetak(), termin.getKraj(),
                stanje(termin, LocalDateTime.now()),
                zadatak.isPresent(),
                resenje.map(Resenje::getVremePreuzimanja).orElse(null),
                resenje.map(Resenje::getVremePredaje).orElse(null),
                resenje.map(Resenje::getId).orElse(null));
    }

    static StanjeTermina stanje(Termin termin, LocalDateTime sada) {
        if (sada.isBefore(termin.getPocetak())) return StanjeTermina.NIJE_POCEO;
        if (sada.isAfter(termin.getKraj())) return StanjeTermina.ZAVRSEN;
        return StanjeTermina.U_TOKU;
    }
}
