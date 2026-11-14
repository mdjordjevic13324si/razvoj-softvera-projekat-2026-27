package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// indeks = smer + broj + godina upisa, npr. RN 12/2023
public record StudentRequest(
        @NotBlank String ime,
        @NotBlank String prezime,
        @NotBlank @Email String email,
        @NotNull Long smerId,
        @Positive int brojIndeksa,
        @Positive int godinaUpisa) {
}
