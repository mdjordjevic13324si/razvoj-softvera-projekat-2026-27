package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Ispit;
import org.raflab.studentski_server.model.PrijavaIspita;
import org.raflab.studentski_server.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PrijavaIspitaRepositoryTest {

    @Autowired
    PrijavaIspitaRepository prijavaIspitaRepository;

    @Autowired
    TestEntityManager entityManager;

    Student student;
    Ispit ispit;

    @BeforeEach
    void pripremiStudentaIIspit() {
        student = TestData.student(entityManager, TestData.smer(entityManager, "RN"), 12, 2023);
        ispit = TestData.ispit(entityManager,
                TestData.ispitniRok(entityManager, "2026-2027", "Januar"),
                TestData.predmet(entityManager, "RS"));
    }

    private PrijavaIspita napraviPrijavu(boolean aktivna) {
        PrijavaIspita prijava = new PrijavaIspita();
        prijava.setStudent(student);
        prijava.setIspit(ispit);
        prijava.setAktivna(aktivna);
        prijava.setVremePromene(LocalDateTime.of(2027, 1, 10, 12, 0));
        return prijava;
    }

    @Test
    void sacuvanaPrijavaSeUcitavaIzBaze() {
        Long id = prijavaIspitaRepository.save(napraviPrijavu(true)).getId();
        entityManager.flush();
        entityManager.clear();

        PrijavaIspita ucitana = prijavaIspitaRepository.findById(id).orElseThrow();
        assertThat(ucitana.getStudent().getId()).isEqualTo(student.getId());
        assertThat(ucitana.getIspit().getId()).isEqualTo(ispit.getId());
        assertThat(ucitana.isAktivna()).isTrue();
        assertThat(ucitana.getVremePromene()).isEqualTo(LocalDateTime.of(2027, 1, 10, 12, 0));
    }

    @Test
    void odjavaMenjaAktivnaAPrijavaOstajeIstiRed() {
        PrijavaIspita prijava = entityManager.persist(napraviPrijavu(true));
        entityManager.flush();

        prijava.setAktivna(false);   // odjava
        prijavaIspitaRepository.saveAndFlush(prijava);
        entityManager.clear();

        PrijavaIspita ucitana = prijavaIspitaRepository.findById(prijava.getId()).orElseThrow();
        assertThat(ucitana.isAktivna()).isFalse();
        assertThat(prijavaIspitaRepository.count()).isEqualTo(1);
    }

    @Test
    void studentNeMozeImatiDvePrijaveZaIstiIspit() {
        entityManager.persist(napraviPrijavu(true));

        assertThatThrownBy(() -> prijavaIspitaRepository.saveAndFlush(napraviPrijavu(true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
