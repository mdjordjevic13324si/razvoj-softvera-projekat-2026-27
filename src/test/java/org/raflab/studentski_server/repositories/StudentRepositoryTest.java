package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Smer;
import org.raflab.studentski_server.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TestEntityManager entityManager;

    Smer smerRn;

    @BeforeEach
    void pripremiSmer() {
        smerRn = TestData.smer(entityManager, "RN");
    }

    @Test
    void sacuvanStudentSeUcitavaIzBazeSaSmerom() {
        Student student = new Student();
        student.setIme("Marko");
        student.setPrezime("Markovic");
        student.setEmail("mmarkovic1223rn@raf.rs");
        student.setSmer(smerRn);
        student.setBrojIndeksa(12);
        student.setGodinaUpisa(2023);
        Long id = studentRepository.save(student).getId();
        entityManager.flush();
        entityManager.clear();

        Student ucitan = studentRepository.findById(id).orElseThrow();
        assertThat(ucitan.getIme()).isEqualTo("Marko");
        assertThat(ucitan.getPrezime()).isEqualTo("Markovic");
        assertThat(ucitan.getEmail()).isEqualTo("mmarkovic1223rn@raf.rs");
        assertThat(ucitan.getSmer().getOznaka()).isEqualTo("RN");
        assertThat(ucitan.getBrojIndeksa()).isEqualTo(12);
        assertThat(ucitan.getGodinaUpisa()).isEqualTo(2023);
    }

    @Test
    void obrisanStudentSeViseNeVidiAliOstajeUTabeli() {
        Student student = TestData.student(entityManager, smerRn, 12, 2023);
        student.setObrisan(true);   // ovo radi DELETE u CRUD-u
        studentRepository.saveAndFlush(student);
        entityManager.clear();

        assertThat(studentRepository.findByIdAndObrisanFalse(student.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(studentRepository.findById(student.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void dvaStudentaNeMoguImatiIstiIndeks() {
        TestData.student(entityManager, smerRn, 12, 2023);
        Student duplikat = new Student();
        duplikat.setIme("Drugi");
        duplikat.setPrezime("Student");
        duplikat.setEmail("drugi@raf.rs");
        duplikat.setSmer(smerRn);
        duplikat.setBrojIndeksa(12);
        duplikat.setGodinaUpisa(2023);

        assertThatThrownBy(() -> studentRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void istiBrojIGodinaNaRazlicitomSmeruJeDrugiIndeks() {
        Smer smerRi = TestData.smer(entityManager, "RI");
        TestData.student(entityManager, smerRn, 12, 2023);
        TestData.student(entityManager, smerRi, 12, 2023);   // RI 12/2023 nije isto sto i RN 12/2023
        entityManager.flush();

        assertThat(studentRepository.count()).isEqualTo(2);
    }
}
