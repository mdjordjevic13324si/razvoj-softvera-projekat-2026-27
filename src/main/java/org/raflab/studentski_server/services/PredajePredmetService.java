package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.PredajePredmetRequest;
import org.raflab.studentski_server.controllers.response.PredajePredmetResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.PredajePredmetMapper;
import org.raflab.studentski_server.model.PredajePredmet;
import org.raflab.studentski_server.repositories.PredajePredmetRepository;
import org.springframework.stereotype.Service;

@Service
public class PredajePredmetService extends CrudService<PredajePredmet, PredajePredmetRequest, PredajePredmetResponse> {

    public PredajePredmetService(PredajePredmetRepository repository, PredajePredmetMapper mapper) {
        super(repository, mapper, "Veza nastavnik-predmet");
    }
}
