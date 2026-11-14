package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;

// uz id ispita idu i predmet i rok, da klijent ne mora da ih trazi posebnim zahtevom
public record TerminResponse(
        Long id,
        Long ispitId,
        String predmetSifra,
        String ispitniRokNaziv,
        String skolskaGodina,
        int redniBroj,
        LocalDateTime pocetak,
        LocalDateTime kraj) {
}
