package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Ispit;
import org.raflab.studentski_server.model.IspitniRok;
import org.raflab.studentski_server.model.Predmet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class IspitRepositoryTest {

    @Autowired
    IspitRepository ispitRepository;

    @Autowired
    TestEntityManager entityManager;

    IspitniRok januar;
    Predmet razvojSoftvera;

    @BeforeEach
    void pripremiRokIPredmet() {
        januar = TestData.ispitniRok(entityManager, "2026-2027", "Januar");
        razvojSoftvera = TestData.predmet(entityManager, "RS");
    }

    @Test
    void sacuvanIspitSeUcitavaIzBazeSaRokomIPredmetom() {
        Ispit ispit = new Ispit();
        ispit.setIspitniRok(januar);
        ispit.setPredmet(razvojSoftvera);
        ispit.setDatum(LocalDate.of(2027, 1, 25));
        Long id = ispitRepository.save(ispit).getId();
        entityManager.flush();
        entityManager.clear();

        Ispit ucitan = ispitRepository.findById(id).orElseThrow();
        assertThat(ucitan.getIspitniRok().getNaziv()).isEqualTo("Januar");
        assertThat(ucitan.getPredmet().getSifra()).isEqualTo("RS");
        assertThat(ucitan.getDatum()).isEqualTo(LocalDate.of(2027, 1, 25));
    }

    @Test
    void obrisanIspitSeViseNeVidiAliOstajeUTabeli() {
        Ispit ispit = TestData.ispit(entityManager, januar, razvojSoftvera);
        ispit.setObrisan(true);   // ovo radi DELETE u CRUD-u
        ispitRepository.saveAndFlush(ispit);
        entityManager.clear();

        assertThat(ispitRepository.findByIdAndObrisanFalse(ispit.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(ispitRepository.findById(ispit.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void predmetNeMozeDvaPutaUIstomRoku() {
        TestData.ispit(entityManager, januar, razvojSoftvera);
        Ispit duplikat = new Ispit();
        duplikat.setIspitniRok(januar);
        duplikat.setPredmet(razvojSoftvera);
        duplikat.setDatum(LocalDate.of(2027, 1, 30));

        assertThatThrownBy(() -> ispitRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
