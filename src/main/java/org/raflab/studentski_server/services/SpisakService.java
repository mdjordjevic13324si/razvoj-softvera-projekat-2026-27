package org.raflab.studentski_server.services;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.SpisakStavkaRequest;
import org.raflab.studentski_server.exceptions.ConflictException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.repositories.GrupaRepository;
import org.raflab.studentski_server.repositories.SpisakStavkaRepository;
import org.raflab.studentski_server.repositories.StudentRepository;
import org.raflab.studentski_server.repositories.UcionicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/*
 * Spisak za polaganje: koji student polaze u kojoj grupi i u kojoj ucionici.
 */
@AllArgsConstructor
@Service
public class SpisakService {

    final SpisakStavkaRepository spisakStavkaRepository;
    final GrupaRepository grupaRepository;
    final StudentRepository studentRepository;
    final UcionicaRepository ucionicaRepository;
    final PristupService pristupService;

    /*
     * Dodaje studente na spisak grupe. Ako je student vec na spisku te grupe, menja mu se samo ucionica.
     * Sve ili nista: ako jedna stavka nije ispravna, nijedna se ne snima.
     */
    @Transactional
    public List<SpisakStavka> unesi(Long grupaId, List<SpisakStavkaRequest> stavke) {
        Grupa grupa = nadjiGrupu(grupaId);
        Ispit ispit = grupa.getTermin().getIspit();
        pristupService.ulogovaniNastavnikKojiPredaje(ispit.getPredmet());

        List<SpisakStavka> sacuvane = new ArrayList<>();
        for (SpisakStavkaRequest zahtev : stavke) {
            String indeks = zahtev.getSmerOznaka() + " " + zahtev.getBrojIndeksa() + "/" + zahtev.getGodinaUpisa();
            Student student = studentRepository
                    .findBySmerOznakaIgnoreCaseAndBrojIndeksaAndGodinaUpisaAndObrisanFalse(
                            zahtev.getSmerOznaka().trim(), zahtev.getBrojIndeksa(), zahtev.getGodinaUpisa())
                    .orElseThrow(() -> new ResourceNotFoundException("Student " + indeks + " ne postoji"));
            Ucionica ucionica = ucionicaRepository.findByOznakaIgnoreCaseAndObrisanFalse(zahtev.getUcionicaOznaka().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Ucionica " + zahtev.getUcionicaOznaka() + " ne postoji"));

            // student polaze ispit u tacno jednoj grupi
            if (spisakStavkaRepository.brojStavkiUDrugimGrupamaIspita(student.getId(), ispit.getId(), grupaId) > 0) {
                throw new ConflictException("Student " + indeks + " je vec na spisku druge grupe za ovaj ispit");
            }

            // par (student, grupa) je jedinstven, pa se postojeca (i obrisana) stavka menja umesto da se pravi nova
            SpisakStavka stavka = spisakStavkaRepository.findByStudentIdAndGrupaId(student.getId(), grupaId)
                    .orElseGet(SpisakStavka::new);
            stavka.setStudent(student);
            stavka.setGrupa(grupa);
            stavka.setUcionica(ucionica);
            stavka.setObrisan(false);
            sacuvane.add(spisakStavkaRepository.save(stavka));
        }
        return sacuvane;
    }

    @Transactional(readOnly = true)
    public List<SpisakStavka> spisakGrupe(Long grupaId) {
        nadjiGrupu(grupaId);
        return spisakStavkaRepository.findAllByGrupaIdAndObrisanFalse(grupaId);
    }

    // skida studenta sa spiska (soft brisanje)
    @Transactional
    public void obrisi(Long grupaId, Long stavkaId) {
        SpisakStavka stavka = spisakStavkaRepository.findByIdAndObrisanFalse(stavkaId)
                .filter(s -> s.getGrupa().getId().equals(grupaId))
                .orElseThrow(() -> new ResourceNotFoundException("Stavka " + stavkaId + " ne postoji na spisku grupe " + grupaId));
        pristupService.ulogovaniNastavnikKojiPredaje(stavka.getGrupa().getTermin().getIspit().getPredmet());
        stavka.setObrisan(true);
        spisakStavkaRepository.save(stavka);
    }

    private Grupa nadjiGrupu(Long grupaId) {
        return grupaRepository.findByIdAndObrisanFalse(grupaId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupa " + grupaId + " ne postoji"));
    }
}
