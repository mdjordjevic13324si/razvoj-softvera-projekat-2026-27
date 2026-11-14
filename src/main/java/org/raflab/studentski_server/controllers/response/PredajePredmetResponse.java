package org.raflab.studentski_server.controllers.response;

public record PredajePredmetResponse(
        Long id,
        Long nastavnikId,
        String nastavnikImeIPrezime,
        Long predmetId,
        String predmetSifra,
        String predmetNaziv) {
}
