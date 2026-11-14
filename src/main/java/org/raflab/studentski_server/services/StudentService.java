package org.raflab.studentski_server.services;

import org.raflab.studentski_server.controllers.request.StudentRequest;
import org.raflab.studentski_server.controllers.response.StudentResponse;
import org.raflab.studentski_server.crud.CrudService;
import org.raflab.studentski_server.mappers.StudentMapper;
import org.raflab.studentski_server.model.Student;
import org.raflab.studentski_server.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService extends CrudService<Student, StudentRequest, StudentResponse> {

    public StudentService(StudentRepository repository, StudentMapper mapper) {
        super(repository, mapper, "Student");
    }
}
