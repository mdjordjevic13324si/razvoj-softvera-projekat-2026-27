package org.raflab.studentski_server.controllers.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SpisakRequest {

    @NotEmpty
    @Valid   // proverava i svaku stavku posebno
    private List<SpisakStavkaRequest> stavke;
}
