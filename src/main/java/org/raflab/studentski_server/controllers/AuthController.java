package org.raflab.studentski_server.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.LoginRequest;
import org.raflab.studentski_server.controllers.response.LoginResponse;
import org.raflab.studentski_server.services.AuthService;
import org.raflab.studentski_server.services.AuthService.Prijava;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/auth")
public class AuthController {

	final AuthService authService;

	@PostMapping(path = "/login")
	public LoginResponse login(@RequestBody @Valid LoginRequest request) {
		Prijava prijava = authService.login(request.getEmail(), request.getLozinka());
		return new LoginResponse(prijava.token(), prijava.istice(), prijava.uloga());
	}
}
