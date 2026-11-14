package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.TerminRequest;
import org.raflab.studentski_server.controllers.response.TerminResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.TerminService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/termini")
public class TerminController extends CrudController<TerminRequest, TerminResponse> {

    public TerminController(TerminService service) {
        super(service);
    }
}
