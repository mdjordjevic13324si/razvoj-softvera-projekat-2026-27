package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;

/*
 * Jedno polaganje u monitoringu studenta
 */
public record MonitoringPolaganjaResponse(
        Long grupaId,
        String grupaOznaka,
        Long terminId,
        int terminRedniBroj,
        String predmetSifra,
        String ispitniRokNaziv,
        String ucionicaOznaka,
        LocalDateTime pocetak,
        LocalDateTime kraj,
        StatusPolaganja status,
        LocalDateTime vremePreuzimanja,
        LocalDateTime vremePredaje,
        Long vremeIzradeSekundi) {
}
