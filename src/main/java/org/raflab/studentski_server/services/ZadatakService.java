package org.raflab.studentski_server.services;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.exceptions.BadRequestException;
import org.raflab.studentski_server.exceptions.ConflictException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.repositories.GrupaRepository;
import org.raflab.studentski_server.repositories.ZadatakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/*
 * Postavljanje zadataka. Zadatak se dodeljuje grupi, a grupa odredjuje termin, ispit, predmet i ispitni rok.
 */
@AllArgsConstructor
@Service
public class ZadatakService {

    final ZadatakRepository zadatakRepository;
    final GrupaRepository grupaRepository;
    final PristupService pristupService;
    final SkladisteService skladisteService;

    @Transactional
    public Zadatak postavi(Long grupaId, String naslov, String opis, MultipartFile fajl) {
        if (naslov == null || naslov.isBlank()) {
            throw new BadRequestException("Naslov je obavezan");
        }
        Grupa grupa = grupaRepository.findByIdAndObrisanFalse(grupaId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupa " + grupaId + " ne postoji"));
        Ispit ispit = grupa.getTermin().getIspit();
        Nastavnik nastavnik = pristupService.ulogovaniNastavnikKojiPredaje(ispit.getPredmet());
        skladisteService.proveriDaJeZip(fajl);

        // kolona grupa_id je jedinstvena, pa se obrisan zadatak iste grupe "ozivljava" umesto da se pravi nov red
        Zadatak zadatak = zadatakRepository.findByGrupaId(grupaId).orElseGet(Zadatak::new);
        if (zadatak.getId() != null && !zadatak.isObrisan()) {
            throw new ConflictException("Grupa vec ima zadatak; obrisite ga pa postavite nov");
        }

        String putanjaZipa = putanjaZadatka(grupa);
        skladisteService.sacuvaj(putanjaZipa, fajl);

        zadatak.setGrupa(grupa);
        zadatak.setNastavnik(nastavnik);
        zadatak.setNaslov(naslov.trim());
        zadatak.setOpis(opis);
        zadatak.setPutanjaZipa(putanjaZipa);
        zadatak.setOriginalnoIme(fajl.getOriginalFilename());
        zadatak.setObrisan(false);
        return zadatakRepository.save(zadatak);
    }

    @Transactional(readOnly = true)
    public Zadatak nadji(Long zadatakId) {
        return zadatakRepository.findByIdAndObrisanFalse(zadatakId)
                .orElseThrow(() -> new ResourceNotFoundException("Zadatak " + zadatakId + " ne postoji"));
    }

    // zip zadatka, za nastavnika (student ga dobija kroz PolaganjeService, uz proveru spiska i termina)
    @Transactional(readOnly = true)
    public PreuzetFajl fajl(Long zadatakId) {
        Zadatak zadatak = nadji(zadatakId);
        return new PreuzetFajl(skladisteService.ucitaj(zadatak.getPutanjaZipa()), imeFajla(zadatak));
    }

    static String imeFajla(Zadatak zadatak) {
        String ime = zadatak.getOriginalnoIme();
        return ime == null || ime.isBlank() ? "zadatak.zip" : ime;
    }

    // soft brisanje - zip ostaje na disku
    @Transactional
    public void obrisi(Long zadatakId) {
        Zadatak zadatak = nadji(zadatakId);
        pristupService.ulogovaniNastavnikKojiPredaje(zadatak.getGrupa().getTermin().getIspit().getPredmet());
        zadatak.setObrisan(true);
        zadatakRepository.save(zadatak);
    }

    // npr. 2026-2027/januar/RS/zadaci/termin-1/grupa-A.zip
    static String putanjaZadatka(Grupa grupa) {
        Termin termin = grupa.getTermin();
        Ispit ispit = termin.getIspit();
        return String.join("/",
                zaPutanju(ispit.getIspitniRok().getSkolskaGodina()),
                zaPutanju(ispit.getIspitniRok().getNaziv()).toLowerCase(),
                zaPutanju(ispit.getPredmet().getSifra()),
                "zadaci",
                "termin-" + termin.getRedniBroj(),
                "grupa-" + zaPutanju(grupa.getOznaka()) + ".zip");
    }

    // u imenima foldera i fajlova ostaju samo slova, cifre i crtica
    static String zaPutanju(String tekst) {
        return tekst.trim().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
