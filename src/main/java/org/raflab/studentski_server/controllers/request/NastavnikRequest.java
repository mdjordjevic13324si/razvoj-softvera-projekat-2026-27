package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NastavnikRequest(
        @NotBlank String ime,
        @NotBlank String prezime,
        @NotBlank @Email String email) {
}
