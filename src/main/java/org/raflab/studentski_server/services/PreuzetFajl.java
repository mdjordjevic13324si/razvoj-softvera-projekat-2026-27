package org.raflab.studentski_server.services;

import org.springframework.core.io.Resource;

/*
 * Fajl iz skladista spreman za slanje klijentu, sa imenom pod kojim ga klijent cuva
 */
public record PreuzetFajl(Resource sadrzaj, String ime) {
}
