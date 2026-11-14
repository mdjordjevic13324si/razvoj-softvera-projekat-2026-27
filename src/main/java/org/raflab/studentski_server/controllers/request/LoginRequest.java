package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String email;
    @NotBlank
    private String lozinka;
}
