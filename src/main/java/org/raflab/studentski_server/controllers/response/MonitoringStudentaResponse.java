package org.raflab.studentski_server.controllers.response;

import java.util.List;

/*
 * Monitoring jednog studenta: sva njegova polaganja sa vremenima preuzimanja i predaje
 */
public record MonitoringStudentaResponse(
        Long studentId,
        String ime,
        String prezime,
        String indeks,
        List<MonitoringPolaganjaResponse> polaganja) {
}
