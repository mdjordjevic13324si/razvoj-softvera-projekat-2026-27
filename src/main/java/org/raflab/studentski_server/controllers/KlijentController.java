package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.StudentRequest;
import org.raflab.studentski_server.controllers.response.StudentResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.crud.CrudService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/klijent")
public class KlijentController extends CrudController<StudentRequest, StudentResponse> {
    protected KlijentController(CrudService<?, StudentRequest, StudentResponse> service) {
        super(service);
    }
}
