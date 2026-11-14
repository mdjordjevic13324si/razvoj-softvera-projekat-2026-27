package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;

/*
 * Jedno polaganje prijavljenog studenta: gde i kada polaze i dokle je stigao.
 * vremePreuzimanja i vremePredaje su null dok student ne preuzme, odnosno ne preda.
 * resenjeId je null dok ne preuzme; sa njim student otvara GET /api/resenja/{resenjeId} (poeni i komentari).
 */
public record PolaganjeResponse(
        Long grupaId,
        String grupaOznaka,
        Long terminId,
        int terminRedniBroj,
        String predmetSifra,
        String predmetNaziv,
        String ispitniRokNaziv,
        String ucionicaOznaka,
        LocalDateTime pocetak,
        LocalDateTime kraj,
        StanjeTermina stanjeTermina,
        boolean zadatakPostavljen,
        LocalDateTime vremePreuzimanja,
        LocalDateTime vremePredaje,
        Long resenjeId) {
}
