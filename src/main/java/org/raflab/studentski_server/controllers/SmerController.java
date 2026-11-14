package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.SmerRequest;
import org.raflab.studentski_server.controllers.response.SmerResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.SmerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/smerovi")
public class SmerController extends CrudController<SmerRequest, SmerResponse> {

    public SmerController(SmerService service) {
        super(service);
    }
}
