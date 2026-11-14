package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotNull;

public record PredajePredmetRequest(
        @NotNull Long nastavnikId,
        @NotNull Long predmetId) {
}
