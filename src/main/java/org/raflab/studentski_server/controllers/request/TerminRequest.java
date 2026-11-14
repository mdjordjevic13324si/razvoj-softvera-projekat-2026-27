package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

// veza ka ispitu se salje kao id; vremena u obliku 2027-01-20T10:00:00
public record TerminRequest(
        @NotNull Long ispitId,
        @Positive int redniBroj,
        @NotNull LocalDateTime pocetak,
        @NotNull LocalDateTime kraj) {
}
