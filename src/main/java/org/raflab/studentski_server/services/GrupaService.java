package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.GrupaRequest;
import org.raflab.studentski_server.controllers.response.GrupaResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.GrupaMapper;
import org.raflab.studentski_server.model.Grupa;
import org.raflab.studentski_server.repositories.GrupaRepository;
import org.springframework.stereotype.Service;

@Service
public class GrupaService extends CrudService<Grupa, GrupaRequest, GrupaResponse> {

    public GrupaService(GrupaRepository repository, GrupaMapper mapper) {
        super(repository, mapper, "Grupa");
    }
}
