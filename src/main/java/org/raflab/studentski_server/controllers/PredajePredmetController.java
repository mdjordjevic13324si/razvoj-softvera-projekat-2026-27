package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.PredajePredmetRequest;
import org.raflab.studentski_server.controllers.response.PredajePredmetResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.PredajePredmetService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/predaje-predmet")
public class PredajePredmetController extends CrudController<PredajePredmetRequest, PredajePredmetResponse> {

    public PredajePredmetController(PredajePredmetService service) {
        super(service);
    }
}
