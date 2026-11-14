package org.raflab.studentski_server.controllers.response;

public record StudentResponse(
        Long id,
        String ime,
        String prezime,
        String email,
        Long smerId,
        String smerOznaka,
        int brojIndeksa,
        int godinaUpisa,
        String indeks) {   // npr. RN 12/2023
}
