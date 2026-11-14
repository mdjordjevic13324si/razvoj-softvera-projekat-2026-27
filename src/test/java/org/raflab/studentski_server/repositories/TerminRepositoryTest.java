package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Ispit;
import org.raflab.studentski_server.model.Termin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TerminRepositoryTest {

    @Autowired
    TerminRepository terminRepository;

    @Autowired
    TestEntityManager entityManager;

    Ispit ispit;

    @BeforeEach
    void pripremiIspit() {
        ispit = TestData.ispit(entityManager,
                TestData.ispitniRok(entityManager, "2026-2027", "Januar"),
                TestData.predmet(entityManager, "RS"));
    }

    @Test
    void sacuvanTerminSeUcitavaIzBaze() {
        Termin termin = new Termin();
        termin.setIspit(ispit);
        termin.setRedniBroj(2);
        termin.setPocetak(LocalDateTime.of(2027, 1, 20, 14, 0));
        termin.setKraj(LocalDateTime.of(2027, 1, 20, 17, 0));
        Long id = terminRepository.save(termin).getId();
        entityManager.flush();
        entityManager.clear();

        Termin ucitan = terminRepository.findById(id).orElseThrow();
        assertThat(ucitan.getIspit().getId()).isEqualTo(ispit.getId());
        assertThat(ucitan.getRedniBroj()).isEqualTo(2);
        assertThat(ucitan.getPocetak()).isEqualTo(LocalDateTime.of(2027, 1, 20, 14, 0));
        assertThat(ucitan.getKraj()).isEqualTo(LocalDateTime.of(2027, 1, 20, 17, 0));
    }

    @Test
    void obrisanTerminSeViseNeVidiAliOstajeUTabeli() {
        Termin termin = TestData.termin(entityManager, ispit, 1);
        termin.setObrisan(true);   // ovo radi DELETE u CRUD-u
        terminRepository.saveAndFlush(termin);
        entityManager.clear();

        assertThat(terminRepository.findByIdAndObrisanFalse(termin.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(terminRepository.findById(termin.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void ispitNeMozeImatiDvaTerminaSaIstimRednimBrojem() {
        TestData.termin(entityManager, ispit, 1);
        Termin duplikat = new Termin();
        duplikat.setIspit(ispit);
        duplikat.setRedniBroj(1);
        duplikat.setPocetak(LocalDateTime.of(2027, 1, 20, 14, 0));
        duplikat.setKraj(LocalDateTime.of(2027, 1, 20, 17, 0));

        assertThatThrownBy(() -> terminRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
