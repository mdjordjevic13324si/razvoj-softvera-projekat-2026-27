package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.TerminRequest;
import org.raflab.studentski_server.controllers.response.TerminResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.TerminMapper;
import org.raflab.studentski_server.model.Termin;
import org.raflab.studentski_server.repositories.TerminRepository;
import org.springframework.stereotype.Service;

@Service
public class TerminService extends CrudService<Termin, TerminRequest, TerminResponse> {

    public TerminService(TerminRepository repository, TerminMapper mapper) {
        super(repository, mapper, "Termin");
    }
}
