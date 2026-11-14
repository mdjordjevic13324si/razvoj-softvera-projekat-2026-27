package org.raflab.studentski_server.controllers.response;

import lombok.Data;

/*
 * Jedan red spiska za polaganje
 */
@Data
public class SpisakStavkaResponse {

    private Long id;
    private Long studentId;
    private String ime;
    private String prezime;
    private String indeks;          // npr. RN 12/2023
    private Long grupaId;
    private String grupaOznaka;
    private String ucionicaOznaka;
}
