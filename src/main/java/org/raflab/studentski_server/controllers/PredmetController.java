package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.PredmetRequest;
import org.raflab.studentski_server.controllers.response.PredmetResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.PredmetService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/predmeti")
public class PredmetController extends CrudController<PredmetRequest, PredmetResponse> {

    public PredmetController(PredmetService service) {
        super(service);
    }
}
