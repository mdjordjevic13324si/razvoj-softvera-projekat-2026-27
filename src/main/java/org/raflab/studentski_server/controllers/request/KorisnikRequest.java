package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.raflab.studentski_server.model.Uloga;

// nalog za login; lozinka stize kao cist tekst, a servis je hesira pre cuvanja
public record KorisnikRequest(
        @NotBlank @Email String email,
        @NotBlank String lozinka,
        @NotNull Uloga uloga,
        Long studentId,        // obavezan za ulogu STUDENT
        Long nastavnikId) {    // obavezan za ulogu NASTAVNIK
}
