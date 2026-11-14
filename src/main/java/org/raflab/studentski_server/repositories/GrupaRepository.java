package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Grupa;

import java.util.List;

public interface GrupaRepository extends ObrisivRepository<Grupa> {

    // sve grupe jednog termina, po oznaci (A, B, C...)
    List<Grupa> findAllByTerminIdAndObrisanFalseOrderByOznaka(Long terminId);
}
