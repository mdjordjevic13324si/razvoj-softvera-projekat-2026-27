package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.UcionicaRequest;
import org.raflab.studentski_server.controllers.response.UcionicaResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.UcionicaMapper;
import org.raflab.studentski_server.model.Ucionica;
import org.raflab.studentski_server.repositories.UcionicaRepository;
import org.springframework.stereotype.Service;

// CRUD metode su nasledjene iz kalupa; posebna logika za ucionice bi se dopisala ovde
@Service
public class UcionicaService extends CrudService<Ucionica, UcionicaRequest, UcionicaResponse> {

    public UcionicaService(UcionicaRepository repository, UcionicaMapper mapper) {
        super(repository, mapper, "Ucionica");
    }
}
