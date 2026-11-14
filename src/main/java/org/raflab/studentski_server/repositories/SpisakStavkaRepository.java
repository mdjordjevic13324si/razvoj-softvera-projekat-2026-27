package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.SpisakStavka;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpisakStavkaRepository extends ObrisivRepository<SpisakStavka> {

    // spisak jedne grupe
    List<SpisakStavka> findAllByGrupaIdAndObrisanFalse(Long grupaId);

    // da li je student na spisku grupe (stavka 4)
    Optional<SpisakStavka> findByStudentIdAndGrupaIdAndObrisanFalse(Long studentId, Long grupaId);

    // sva polaganja jednog studenta
    List<SpisakStavka> findAllByStudentIdAndObrisanFalse(Long studentId);

    // vraca i obrisanu stavku, jer je par (student, grupa) jedinstven u tabeli
    Optional<SpisakStavka> findByStudentIdAndGrupaId(Long studentId, Long grupaId);

    // u koliko DRUGIH grupa istog ispita je student vec na spisku (treba da bude 0)
    @Query("""
            select count(s) from SpisakStavka s
            where s.student.id = :studentId
              and s.grupa.termin.ispit.id = :ispitId
              and s.grupa.id <> :grupaId
              and s.obrisan = false
            """)
    long brojStavkiUDrugimGrupamaIspita(@Param("studentId") Long studentId,
                                        @Param("ispitId") Long ispitId,
                                        @Param("grupaId") Long grupaId);
}
