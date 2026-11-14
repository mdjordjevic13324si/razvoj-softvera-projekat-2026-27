package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.NastavnikRequest;
import org.raflab.studentski_server.controllers.response.NastavnikResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.NastavnikMapper;
import org.raflab.studentski_server.model.Nastavnik;
import org.raflab.studentski_server.repositories.NastavnikRepository;
import org.springframework.stereotype.Service;

@Service
public class NastavnikService extends CrudService<Nastavnik, NastavnikRequest, NastavnikResponse> {

    public NastavnikService(NastavnikRepository repository, NastavnikMapper mapper) {
        super(repository, mapper, "Nastavnik");
    }
}
