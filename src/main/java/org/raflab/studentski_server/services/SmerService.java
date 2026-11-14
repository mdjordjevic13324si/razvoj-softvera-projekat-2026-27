package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.SmerRequest;
import org.raflab.studentski_server.controllers.response.SmerResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.SmerMapper;
import org.raflab.studentski_server.model.Smer;
import org.raflab.studentski_server.repositories.SmerRepository;
import org.springframework.stereotype.Service;

@Service
public class SmerService extends CrudService<Smer, SmerRequest, SmerResponse> {

    public SmerService(SmerRepository repository, SmerMapper mapper) {
        super(repository, mapper, "Smer");
    }
}
