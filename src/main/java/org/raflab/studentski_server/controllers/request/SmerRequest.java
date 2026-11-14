package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.raflab.studentski_server.model.VrstaSmera;

public record SmerRequest(
        @NotBlank String oznaka,
        @NotBlank String naziv,
        @NotNull VrstaSmera vrsta) {
}
