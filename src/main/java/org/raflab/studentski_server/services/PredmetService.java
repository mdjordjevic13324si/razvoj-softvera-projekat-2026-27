package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.PredmetRequest;
import org.raflab.studentski_server.controllers.response.PredmetResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.PredmetMapper;
import org.raflab.studentski_server.model.Predmet;
import org.raflab.studentski_server.repositories.PredmetRepository;
import org.springframework.stereotype.Service;

@Service
public class PredmetService extends CrudService<Predmet, PredmetRequest, PredmetResponse> {

    public PredmetService(PredmetRepository repository, PredmetMapper mapper) {
        super(repository, mapper, "Predmet");
    }
}
