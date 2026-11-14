package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.GrupaRequest;
import org.raflab.studentski_server.controllers.response.GrupaResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.GrupaService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/grupe")
public class GrupaController extends CrudController<GrupaRequest, GrupaResponse> {

    public GrupaController(GrupaService service) {
        super(service);
    }
}
