package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Resenje sa poenima i komentarima - isti odgovor dobijaju i nastavnik i student (student samo za svoje).
 * poeni su null dok resenje nije ocenjeno.
 */
public record ResenjeResponse(
        Long id,
        Long studentId,
        String ime,
        String prezime,
        String indeks,
        Long grupaId,
        String grupaOznaka,
        Long terminId,
        int terminRedniBroj,
        String predmetSifra,
        String ispitniRokNaziv,
        String skolskaGodina,
        LocalDateTime vremePreuzimanja,
        LocalDateTime vremePredaje,
        boolean predato,
        Integer poeni,
        List<KomentarResponse> komentari) {
}
