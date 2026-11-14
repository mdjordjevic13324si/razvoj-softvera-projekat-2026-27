package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Ucionica;

import java.util.Optional;

public interface UcionicaRepository extends ObrisivRepository<Ucionica> {

    Optional<Ucionica> findByOznakaIgnoreCaseAndObrisanFalse(String oznaka);
}
