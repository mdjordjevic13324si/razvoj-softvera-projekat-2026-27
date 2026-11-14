package org.raflab.studentski_server.controllers.response;

import org.raflab.studentski_server.model.Uloga;

// namerno bez lozinke - ni heširana ne izlazi iz servera
public record KorisnikResponse(
        Long id,
        String email,
        Uloga uloga,
        Long studentId,
        Long nastavnikId) {
}
