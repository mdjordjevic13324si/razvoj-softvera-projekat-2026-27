package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Resenje;
import org.raflab.studentski_server.model.Student;
import org.raflab.studentski_server.model.Zadatak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ResenjeRepositoryTest {

    @Autowired
    ResenjeRepository resenjeRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TestEntityManager entityManager;

    Student student;
    Zadatak zadatak;

    @BeforeEach
    void pripremiStudentaIZadatak() {
        student = TestData.student(entityManager, TestData.smer(entityManager, "RN"), 12, 2023);
        zadatak = TestData.zadatak(entityManager,
                TestData.grupa(entityManager, TestData.termin(entityManager, TestData.ispit(entityManager,
                        TestData.ispitniRok(entityManager, "2026-2027", "Januar"),
                        TestData.predmet(entityManager, "RS")), 1), "A"),
                TestData.nastavnik(entityManager, "pperic@raf.rs"));
    }

    @Test
    void preuzetoAJosNepredatoResenjeImaPraznaPoljaPredaje() {
        Resenje resenje = new Resenje();
        resenje.setStudent(student);
        resenje.setZadatak(zadatak);
        resenje.setVremePreuzimanja(LocalDateTime.of(2027, 1, 20, 10, 5));
        Long id = resenjeRepository.save(resenje).getId();
        entityManager.flush();
        entityManager.clear();

        Resenje ucitano = resenjeRepository.findById(id).orElseThrow();
        assertThat(ucitano.getVremePreuzimanja()).isEqualTo(LocalDateTime.of(2027, 1, 20, 10, 5));
        assertThat(ucitano.getVremePredaje()).isNull();
        assertThat(ucitano.getPutanjaZipa()).isNull();
        assertThat(ucitano.getPoeni()).isNull();
    }

    @Test
    void predatoIOcenjenoResenjeCuvaVremePutanjuIPoene() {
        Resenje resenje = TestData.resenje(entityManager, student, zadatak);
        resenje.setVremePredaje(LocalDateTime.of(2027, 1, 20, 12, 40));
        resenje.setPutanjaZipa("2026-2027/januar/RS/resenja/termin-1/RN-12-2023.zip");
        resenje.setPoeni(18);
        resenjeRepository.saveAndFlush(resenje);
        entityManager.clear();

        Resenje ucitano = resenjeRepository.findById(resenje.getId()).orElseThrow();
        assertThat(ucitano.getVremePredaje()).isEqualTo(LocalDateTime.of(2027, 1, 20, 12, 40));
        assertThat(ucitano.getPutanjaZipa()).isEqualTo("2026-2027/januar/RS/resenja/termin-1/RN-12-2023.zip");
        assertThat(ucitano.getPoeni()).isEqualTo(18);
    }

    @Test
    void obrisanoResenjeSeViseNeVidiAliOstajeUTabeli() {
        Resenje resenje = TestData.resenje(entityManager, student, zadatak);
        resenje.setObrisan(true);   // ovo radi DELETE u CRUD-u
        resenjeRepository.saveAndFlush(resenje);
        entityManager.clear();

        assertThat(resenjeRepository.findByIdAndObrisanFalse(resenje.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(resenjeRepository.findById(resenje.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void studentNeMozeImatiDvaResenjaZaIstiZadatak() {
        TestData.resenje(entityManager, student, zadatak);
        Resenje drugo = new Resenje();
        drugo.setStudent(student);
        drugo.setZadatak(zadatak);
        drugo.setVremePreuzimanja(LocalDateTime.of(2027, 1, 20, 10, 30));

        assertThatThrownBy(() -> resenjeRepository.saveAndFlush(drugo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void resenjeSeIdaljeUcitavaKadJeStudentObrisan() {
        Resenje resenje = TestData.resenje(entityManager, student, zadatak);
        student.setObrisan(true);   // DELETE studenta u CRUD-u
        studentRepository.saveAndFlush(student);
        entityManager.clear();

        // istorija ne sme da nestane: resenje i dalje postoji i zna ciji je
        Resenje ucitano = resenjeRepository.findById(resenje.getId()).orElseThrow();
        assertThat(ucitano.getStudent().getId()).isEqualTo(student.getId());
        assertThat(ucitano.getStudent().isObrisan()).isTrue();
    }
}
