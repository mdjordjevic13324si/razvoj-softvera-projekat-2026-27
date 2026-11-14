package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;

/*
 * Potvrda uspesne predaje resenja
 */
public record PredajaResponse(
        Long resenjeId,
        String indeks,              // npr. RN 12/2023
        String predmetSifra,
        int terminRedniBroj,
        String grupaOznaka,
        LocalDateTime vremePreuzimanja,
        LocalDateTime vremePredaje,
        long velicinaBajtova) {
}
