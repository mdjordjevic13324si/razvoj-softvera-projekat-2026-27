package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PoeniRequest(
        @NotNull @Min(0) @Max(100) Integer poeni) {
}
