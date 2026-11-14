package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Grupa;
import org.raflab.studentski_server.model.SpisakStavka;
import org.raflab.studentski_server.model.Student;
import org.raflab.studentski_server.model.Ucionica;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SpisakStavkaRepositoryTest {

    @Autowired
    SpisakStavkaRepository spisakStavkaRepository;

    @Autowired
    TestEntityManager entityManager;

    Student student;
    Grupa grupa;
    Ucionica ucionica;

    @BeforeEach
    void pripremiStudentaGrupuIUcionicu() {
        student = TestData.student(entityManager, TestData.smer(entityManager, "RN"), 12, 2023);
        grupa = TestData.grupa(entityManager, TestData.termin(entityManager, TestData.ispit(entityManager,
                TestData.ispitniRok(entityManager, "2026-2027", "Januar"),
                TestData.predmet(entityManager, "RS")), 1), "A");
        ucionica = TestData.ucionica(entityManager, "RAF1");
    }

    private SpisakStavka napraviStavku() {
        SpisakStavka stavka = new SpisakStavka();
        stavka.setStudent(student);
        stavka.setGrupa(grupa);
        stavka.setUcionica(ucionica);
        return stavka;
    }

    @Test
    void sacuvanaStavkaSpiskaSeUcitavaIzBaze() {
        Long id = spisakStavkaRepository.save(napraviStavku()).getId();
        entityManager.flush();
        entityManager.clear();

        SpisakStavka ucitana = spisakStavkaRepository.findById(id).orElseThrow();
        assertThat(ucitana.getStudent().getId()).isEqualTo(student.getId());
        assertThat(ucitana.getGrupa().getOznaka()).isEqualTo("A");
        assertThat(ucitana.getUcionica().getOznaka()).isEqualTo("RAF1");
    }

    @Test
    void obrisanaStavkaSeViseNeVidiAliOstajeUTabeli() {
        SpisakStavka stavka = entityManager.persist(napraviStavku());
        stavka.setObrisan(true);   // ovo radi DELETE u CRUD-u
        spisakStavkaRepository.saveAndFlush(stavka);
        entityManager.clear();

        assertThat(spisakStavkaRepository.findByIdAndObrisanFalse(stavka.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(spisakStavkaRepository.findById(stavka.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void studentNeMozeDvaPutaBitiUIstojGrupi() {
        entityManager.persist(napraviStavku());

        assertThatThrownBy(() -> spisakStavkaRepository.saveAndFlush(napraviStavku()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
