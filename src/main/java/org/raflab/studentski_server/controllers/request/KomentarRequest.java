package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// komentar na odredjenu liniju odredjenog fajla u resenju
public record KomentarRequest(
        @NotBlank @Size(max = 255) String putanjaFajla,   // putanja unutar projekta, npr. src/main/java/Main.java
        @Positive int linija,
        @NotBlank @Size(max = 2000) String tekst) {
}
