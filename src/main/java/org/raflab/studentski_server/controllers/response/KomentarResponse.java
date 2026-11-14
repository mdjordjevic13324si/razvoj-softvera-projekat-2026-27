package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;

public record KomentarResponse(
        Long id,
        String putanjaFajla,
        int linija,
        String tekst,
        LocalDateTime vreme,
        Long nastavnikId,
        String nastavnikImeIPrezime) {
}
