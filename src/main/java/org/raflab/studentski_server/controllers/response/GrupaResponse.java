package org.raflab.studentski_server.controllers.response;

public record GrupaResponse(
        Long id,
        String oznaka,
        Long terminId,
        int terminRedniBroj,
        Long ispitId,
        String predmetSifra) {
}
