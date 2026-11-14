package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record UcionicaRequest(
        @NotBlank String oznaka,
        @NotBlank String adresa,
        int sprat,
        @Positive int kapacitet,
        @PositiveOrZero int brojRacunara) {
}
