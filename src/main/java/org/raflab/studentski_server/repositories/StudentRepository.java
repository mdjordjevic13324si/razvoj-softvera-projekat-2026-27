package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Student;

import java.util.Optional;

public interface StudentRepository extends ObrisivRepository<Student> {

    // student po indeksu, npr. RN 12/2023
    Optional<Student> findBySmerOznakaIgnoreCaseAndBrojIndeksaAndGodinaUpisaAndObrisanFalse(
            String smerOznaka, int brojIndeksa, int godinaUpisa);
}
