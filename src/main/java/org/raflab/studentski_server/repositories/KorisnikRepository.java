package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Korisnik;

import java.util.Optional;

public interface KorisnikRepository extends ObrisivRepository<Korisnik> {

    // za login: obrisan korisnik se ponasa kao da ne postoji
    Optional<Korisnik> findByEmailIgnoreCaseAndObrisanFalse(String email);
}
