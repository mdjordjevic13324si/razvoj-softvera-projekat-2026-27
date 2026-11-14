package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Resenje;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResenjeRepository extends ObrisivRepository<Resenje> {

    // jedno resenje po studentu i zadatku; vraca i obrisano, jer je par (student, zadatak) jedinstven
    Optional<Resenje> findByStudentIdAndZadatakId(Long studentId, Long zadatakId);

    // sva resenja jednog zadatka (monitoring grupe)
    List<Resenje> findAllByZadatakIdAndObrisanFalse(Long zadatakId);

    // sva predata resenja iz predmeta koje dati nastavnik predaje (stavka 7)
    @Query("""
            select r from Resenje r
            where r.obrisan = false
              and r.putanjaZipa is not null
              and r.zadatak.grupa.termin.ispit.predmet.id in (
                    select pp.predmet.id from PredajePredmet pp
                    where pp.nastavnik.id = :nastavnikId and pp.obrisan = false)
            order by r.id
            """)
    List<Resenje> predataResenjaNastavnika(@Param("nastavnikId") Long nastavnikId);
}
