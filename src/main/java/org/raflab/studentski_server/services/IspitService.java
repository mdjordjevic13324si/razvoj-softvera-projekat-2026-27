package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.IspitRequest;
import org.raflab.studentski_server.controllers.response.IspitResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.IspitMapper;
import org.raflab.studentski_server.model.Ispit;
import org.raflab.studentski_server.repositories.IspitRepository;
import org.springframework.stereotype.Service;

@Service
public class IspitService extends CrudService<Ispit, IspitRequest, IspitResponse> {

    public IspitService(IspitRepository repository, IspitMapper mapper) {
        super(repository, mapper, "Ispit");
    }
}
