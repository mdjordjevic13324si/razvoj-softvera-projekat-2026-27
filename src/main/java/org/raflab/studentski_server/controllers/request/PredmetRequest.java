package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;

public record PredmetRequest(
        @NotBlank String sifra,
        @NotBlank String naziv) {
}
