package org.raflab.studentski_server.controllers.response;

import java.time.LocalDate;

public record IspitResponse(
        Long id,
        Long ispitniRokId,
        String ispitniRokNaziv,
        String skolskaGodina,
        Long predmetId,
        String predmetSifra,
        String predmetNaziv,
        LocalDate datum) {
}
