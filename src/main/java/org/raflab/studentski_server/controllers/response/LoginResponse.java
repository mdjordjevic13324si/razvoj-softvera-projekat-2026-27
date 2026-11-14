package org.raflab.studentski_server.controllers.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.raflab.studentski_server.model.Uloga;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;      // salje se kao "Authorization: Bearer <token>"
    private Instant istice;
    private Uloga uloga;
}
