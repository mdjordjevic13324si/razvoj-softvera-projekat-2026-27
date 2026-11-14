package org.raflab.studentski_server.controllers;

import org.raflab.studentski_server.controllers.request.StudentRequest;
import org.raflab.studentski_server.controllers.response.StudentResponse;
import org.raflab.studentski_server.crud.CrudController;
import org.raflab.studentski_server.services.StudentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/studenti")
public class StudentController extends CrudController<StudentRequest, StudentResponse> {

    public StudentController(StudentService service) {
        super(service);
    }
}
