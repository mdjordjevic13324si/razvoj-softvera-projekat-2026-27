package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Zadatak;

import java.util.Optional;

public interface ZadatakRepository extends ObrisivRepository<Zadatak> {

    // jedan zadatak po grupi; vraca i obrisan zadatak, jer je kolona grupa_id jedinstvena
    Optional<Zadatak> findByGrupaId(Long grupaId);
}
