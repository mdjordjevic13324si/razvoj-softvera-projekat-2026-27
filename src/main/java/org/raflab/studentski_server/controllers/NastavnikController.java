package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.NastavnikRequest;
import org.raflab.studentski_server.controllers.response.NastavnikResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.NastavnikService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/nastavnici")
public class NastavnikController extends CrudController<NastavnikRequest, NastavnikResponse> {

    public NastavnikController(NastavnikService service) {
        super(service);
    }
}
