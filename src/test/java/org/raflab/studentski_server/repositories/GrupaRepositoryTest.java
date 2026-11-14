package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Grupa;
import org.raflab.studentski_server.model.Termin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class GrupaRepositoryTest {

    @Autowired
    GrupaRepository grupaRepository;

    @Autowired
    TestEntityManager entityManager;

    Termin termin;

    @BeforeEach
    void pripremiTermin() {
        termin = TestData.termin(entityManager, TestData.ispit(entityManager,
                TestData.ispitniRok(entityManager, "2026-2027", "Januar"),
                TestData.predmet(entityManager, "RS")), 1);
    }

    @Test
    void sacuvanaGrupaSeUcitavaIzBaze() {
        Grupa grupa = new Grupa();
        grupa.setTermin(termin);
        grupa.setOznaka("A");
        Long id = grupaRepository.save(grupa).getId();
        entityManager.flush();
        entityManager.clear();

        Grupa ucitana = grupaRepository.findById(id).orElseThrow();
        assertThat(ucitana.getOznaka()).isEqualTo("A");
        assertThat(ucitana.getTermin().getId()).isEqualTo(termin.getId());
    }

    @Test
    void obrisanaGrupaSeViseNeVidiAliOstajeUTabeli() {
        Grupa grupa = TestData.grupa(entityManager, termin, "A");
        grupa.setObrisan(true);   // ovo radi DELETE u CRUD-u
        grupaRepository.saveAndFlush(grupa);
        entityManager.clear();

        assertThat(grupaRepository.findByIdAndObrisanFalse(grupa.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(grupaRepository.findById(grupa.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void terminNeMozeImatiDveGrupeSaIstomOznakom() {
        TestData.grupa(entityManager, termin, "A");
        Grupa duplikat = new Grupa();
        duplikat.setTermin(termin);
        duplikat.setOznaka("A");

        assertThatThrownBy(() -> grupaRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
