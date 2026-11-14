package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.IspitniRokRequest;
import org.raflab.studentski_server.controllers.response.IspitniRokResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.IspitniRokMapper;
import org.raflab.studentski_server.model.IspitniRok;
import org.raflab.studentski_server.repositories.IspitniRokRepository;
import org.springframework.stereotype.Service;

@Service
public class IspitniRokService extends CrudService<IspitniRok, IspitniRokRequest, IspitniRokResponse> {

    public IspitniRokService(IspitniRokRepository repository, IspitniRokMapper mapper) {
        super(repository, mapper, "Ispitni rok");
    }
}
