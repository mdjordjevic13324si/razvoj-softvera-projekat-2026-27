package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.IspitRequest;
import org.raflab.studentski_server.controllers.response.IspitResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.IspitService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/ispiti")
public class IspitController extends CrudController<IspitRequest, IspitResponse> {

    public IspitController(IspitService service) {
        super(service);
    }
}
