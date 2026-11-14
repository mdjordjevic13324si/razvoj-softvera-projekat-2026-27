package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// datum u obliku 2027-01-20
public record IspitRequest(
        @NotNull Long ispitniRokId,
        @NotNull Long predmetId,
        @NotNull LocalDate datum) {
}
