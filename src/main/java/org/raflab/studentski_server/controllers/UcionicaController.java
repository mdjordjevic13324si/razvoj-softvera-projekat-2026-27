package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.UcionicaRequest;
import org.raflab.studentski_server.controllers.response.UcionicaResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.UcionicaService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// svih pet endpointa je nasledjeno iz kalupa
@RestController
@RequestMapping(path = "/api/ucionice")
public class UcionicaController extends CrudController<UcionicaRequest, UcionicaResponse> {

    public UcionicaController(UcionicaService service) {
        super(service);
    }
}
