package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrupaRequest(
        @NotNull Long terminId,
        @NotBlank String oznaka) {   // A, B, C...
}
