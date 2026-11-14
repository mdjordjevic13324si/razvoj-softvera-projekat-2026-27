package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.IspitniRokRequest;
import org.raflab.studentski_server.controllers.response.IspitniRokResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.IspitniRokService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/ispitni-rokovi")
public class IspitniRokController extends CrudController<IspitniRokRequest, IspitniRokResponse> {

    public IspitniRokController(IspitniRokService service) {
        super(service);
    }
}
