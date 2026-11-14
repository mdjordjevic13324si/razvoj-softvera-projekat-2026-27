package org.raflab.studentski_server.services;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.KomentarRequest;
import org.raflab.studentski_server.controllers.response.KomentarResponse;
import org.raflab.studentski_server.controllers.response.ResenjeResponse;
import org.raflab.studentski_server.exceptions.ConflictException;
import org.raflab.studentski_server.exceptions.ForbiddenException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.repositories.KomentarRepository;
import org.raflab.studentski_server.repositories.ResenjeRepository;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 * Pregled i ocenjivanje: nastavnik preuzima predata resenja i upisuje komentare i poene,
 * a student vidi poene i komentare samo za svoje resenje.
 */
@AllArgsConstructor
@Service
public class OcenjivanjeService {

    final ResenjeRepository resenjeRepository;
    final KomentarRepository komentarRepository;
    final PristupService pristupService;
    final SkladisteService skladisteService;

    // ---------- citanje ----------

    // predata resenja po predmetu, roku, terminu i grupi (svaki kriterijum je opcion)
    @Transactional(readOnly = true)
    public List<ResenjeResponse> pretrazi(Long predmetId, Long ispitniRokId, Long terminId, Long grupaId) {
        return pretraziPredata(predmetId, ispitniRokId, terminId, grupaId).stream()
                .map(this::toResponse)
                .toList();
    }

    // jedno resenje: nastavnik koji predaje predmet, ili student kome resenje pripada
    @Transactional(readOnly = true)
    public ResenjeResponse nadji(Long resenjeId) {
        Resenje resenje = nadjiResenje(resenjeId);
        pristupService.proveriCitanjeResenja(resenje);
        return toResponse(resenje);
    }

    // zip jednog resenja, za nastavnika
    @Transactional(readOnly = true)
    public PreuzetFajl fajl(Long resenjeId) {
        Resenje resenje = nadjiResenje(resenjeId);
        pristupService.ulogovaniNastavnikKojiPredaje(predmet(resenje));
        if (resenje.getPutanjaZipa() == null) {
            throw new ResourceNotFoundException("Resenje jos nije predato");
        }
        return new PreuzetFajl(skladisteService.ucitaj(resenje.getPutanjaZipa()),
                PolaganjeService.imeFajlaResenja(resenje.getStudent()));
    }

    /*
     * Sva predata resenja po kriterijumima u jednom zipu. Unutar njega svako resenje zadrzava
     * putanju iz skladista, npr. 2026-2027/januar/RS/resenja/termin-1/grupa-A/RN-12-2023.zip
     */
    @Transactional(readOnly = true)
    public PreuzetFajl zip(Long predmetId, Long ispitniRokId, Long terminId, Long grupaId) {
        List<Resenje> resenja = pretraziPredata(predmetId, ispitniRokId, terminId, grupaId);
        if (resenja.isEmpty()) {
            throw new ResourceNotFoundException("Nema predatih resenja za zadate kriterijume");
        }
        ByteArrayOutputStream bajtovi = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bajtovi)) {
            for (Resenje resenje : resenja) {
                zip.putNextEntry(new ZipEntry(resenje.getPutanjaZipa()));
                try (InputStream sadrzaj = skladisteService.ucitaj(resenje.getPutanjaZipa()).getInputStream()) {
                    sadrzaj.transferTo(zip);
                }
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Zip sa resenjima nije napravljen", e);
        }
        return new PreuzetFajl(new ByteArrayResource(bajtovi.toByteArray()), "resenja.zip");
    }

    // ---------- poeni ----------

    @Transactional
    public ResenjeResponse upisiPoene(Long resenjeId, int poeni) {
        Resenje resenje = nadjiResenje(resenjeId);
        pristupService.ulogovaniNastavnikKojiPredaje(predmet(resenje));
        resenje.setPoeni(poeni);
        return toResponse(resenjeRepository.save(resenje));
    }

    // ---------- komentari ----------

    @Transactional
    public KomentarResponse dodajKomentar(Long resenjeId, KomentarRequest request) {
        Resenje resenje = nadjiResenje(resenjeId);
        Nastavnik nastavnik = pristupService.ulogovaniNastavnikKojiPredaje(predmet(resenje));
        if (resenje.getPutanjaZipa() == null) {
            throw new ConflictException("Resenje jos nije predato, pa nema fajlova za komentarisanje");
        }
        Komentar komentar = new Komentar();
        komentar.setResenje(resenje);
        komentar.setNastavnik(nastavnik);
        komentar.setVreme(LocalDateTime.now());
        popuni(komentar, request);
        return toKomentarResponse(komentarRepository.save(komentar));
    }

    @Transactional
    public KomentarResponse izmeniKomentar(Long resenjeId, Long komentarId, KomentarRequest request) {
        Komentar komentar = nadjiKomentar(resenjeId, komentarId);
        pristupService.ulogovaniNastavnikKojiPredaje(predmet(komentar.getResenje()));
        popuni(komentar, request);
        return toKomentarResponse(komentarRepository.save(komentar));
    }

    @Transactional
    public void obrisiKomentar(Long resenjeId, Long komentarId) {
        Komentar komentar = nadjiKomentar(resenjeId, komentarId);
        pristupService.ulogovaniNastavnikKojiPredaje(predmet(komentar.getResenje()));
        komentar.setObrisan(true);
        komentarRepository.save(komentar);
    }

    // ---------- pomocne metode ----------

    private List<Resenje> pretraziPredata(Long predmetId, Long ispitniRokId, Long terminId, Long grupaId) {
        Long nastavnikId = pristupService.ulogovaniNastavnikId();
        if (nastavnikId == null) {
            throw new ForbiddenException("Ova akcija je dozvoljena samo nastavniku");
        }
        // kriterijum koji je null se preskace; filtrira se u Javi, jer je spisak resenja jednog nastavnika mali
        return resenjeRepository.predataResenjaNastavnika(nastavnikId).stream()
                .filter(resenje -> {
                    Termin termin = resenje.getZadatak().getGrupa().getTermin();
                    Ispit ispit = termin.getIspit();
                    return odgovara(predmetId, ispit.getPredmet().getId())
                            && odgovara(ispitniRokId, ispit.getIspitniRok().getId())
                            && odgovara(terminId, termin.getId())
                            && odgovara(grupaId, resenje.getZadatak().getGrupa().getId());
                })
                .toList();
    }

    // kriterijum null znaci "bez tog filtera"
    private static boolean odgovara(Long trazeniId, Long stvarniId) {
        return trazeniId == null || trazeniId.equals(stvarniId);
    }

    private Resenje nadjiResenje(Long resenjeId) {
        return resenjeRepository.findByIdAndObrisanFalse(resenjeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resenje " + resenjeId + " ne postoji"));
    }

    // komentar mora da pripada bas tom resenju
    private Komentar nadjiKomentar(Long resenjeId, Long komentarId) {
        return komentarRepository.findByIdAndObrisanFalse(komentarId)
                .filter(komentar -> komentar.getResenje().getId().equals(resenjeId) && !komentar.getResenje().isObrisan())
                .orElseThrow(() -> new ResourceNotFoundException("Komentar " + komentarId + " ne postoji na resenju " + resenjeId));
    }

    private static Predmet predmet(Resenje resenje) {
        return resenje.getZadatak().getGrupa().getTermin().getIspit().getPredmet();
    }

    private static void popuni(Komentar komentar, KomentarRequest request) {
        komentar.setPutanjaFajla(request.putanjaFajla().trim());
        komentar.setLinija(request.linija());
        komentar.setTekst(request.tekst().trim());
    }

    private ResenjeResponse toResponse(Resenje resenje) {
        Student student = resenje.getStudent();
        Grupa grupa = resenje.getZadatak().getGrupa();
        Termin termin = grupa.getTermin();
        Ispit ispit = termin.getIspit();
        List<KomentarResponse> komentari = komentarRepository
                .findAllByResenjeIdAndObrisanFalseOrderByPutanjaFajlaAscLinijaAscIdAsc(resenje.getId()).stream()
                .map(OcenjivanjeService::toKomentarResponse)
                .toList();

        return new ResenjeResponse(resenje.getId(),
                student.getId(), student.getIme(), student.getPrezime(), Converters.indeks(student),
                grupa.getId(), grupa.getOznaka(),
                termin.getId(), termin.getRedniBroj(),
                ispit.getPredmet().getSifra(), ispit.getIspitniRok().getNaziv(), ispit.getIspitniRok().getSkolskaGodina(),
                resenje.getVremePreuzimanja(), resenje.getVremePredaje(),
                resenje.getPutanjaZipa() != null,
                resenje.getPoeni(),
                komentari);
    }

    private static KomentarResponse toKomentarResponse(Komentar komentar) {
        Nastavnik nastavnik = komentar.getNastavnik();
        return new KomentarResponse(komentar.getId(), komentar.getPutanjaFajla(), komentar.getLinija(),
                komentar.getTekst(), komentar.getVreme(),
                nastavnik.getId(), nastavnik.getIme() + " " + nastavnik.getPrezime());
    }
}
