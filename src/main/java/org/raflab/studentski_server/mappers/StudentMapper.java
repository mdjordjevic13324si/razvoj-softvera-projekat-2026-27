package org.raflab.studentski_server.mappers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.StudentRequest;
import org.raflab.studentski_server.controllers.response.StudentResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.Smer;
import org.raflab.studentski_server.model.Student;
import org.raflab.studentski_server.repositories.SmerRepository;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StudentMapper implements CrudMapper<Student, StudentRequest, StudentResponse> {

    final SmerRepository smerRepository;

    @Override
    public Student createEntityFromRequest(StudentRequest request) {
        Student student = new Student();
        updateExistingEntityFromRequest(student, request);
        return student;
    }

    @Override
    public void updateExistingEntityFromRequest(Student student, StudentRequest request) {
        Smer smer = smerRepository.findByIdAndObrisanFalse(request.smerId())
                .orElseThrow(() -> new ResourceNotFoundException("Smer " + request.smerId() + " ne postoji"));
        student.setIme(request.ime().trim());
        student.setPrezime(request.prezime().trim());
        student.setEmail(request.email().trim());
        student.setSmer(smer);
        student.setBrojIndeksa(request.brojIndeksa());
        student.setGodinaUpisa(request.godinaUpisa());
    }

    @Override
    public StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getIme(), student.getPrezime(), student.getEmail(),
                student.getSmer().getId(), student.getSmer().getOznaka(),
                student.getBrojIndeksa(), student.getGodinaUpisa(), Converters.indeks(student));
    }
}
