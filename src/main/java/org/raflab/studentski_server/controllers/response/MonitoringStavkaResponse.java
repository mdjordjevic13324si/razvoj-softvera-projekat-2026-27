package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;

/*
 * Jedan student u monitoringu grupe.
 * vremeIzradeSekundi = vreme predaje - vreme preuzimanja; null dok student ne preda.
 */
public record MonitoringStavkaResponse(
        Long studentId,
        String ime,
        String prezime,
        String indeks,
        String ucionicaOznaka,
        StatusPolaganja status,
        LocalDateTime vremePreuzimanja,
        LocalDateTime vremePredaje,
        Long vremeIzradeSekundi) {
}
