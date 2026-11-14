package org.raflab.studentski_server.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/*
 * Jedan student na spisku: indeks (smer + broj + godina upisa) i ucionica u kojoj polaze
 */
@Data
public class SpisakStavkaRequest {

    @NotBlank
    private String smerOznaka;      // RN
    @NotNull
    private Integer brojIndeksa;    // 12
    @NotNull
    private Integer godinaUpisa;    // 2023
    @NotBlank
    private String ucionicaOznaka;  // RAF1
}
