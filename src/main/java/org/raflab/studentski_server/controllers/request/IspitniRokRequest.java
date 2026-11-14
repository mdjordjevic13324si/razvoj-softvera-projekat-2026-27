package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.raflab.studentski_server.model.TipRoka;

public record IspitniRokRequest(
        @NotBlank String skolskaGodina,   // 2026-2027
        @NotBlank String naziv,           // Januar, Prvi kolokvijum...
        @NotNull TipRoka tip) {
}
