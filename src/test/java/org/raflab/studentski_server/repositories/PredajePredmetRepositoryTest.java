package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Nastavnik;
import org.raflab.studentski_server.model.PredajePredmet;
import org.raflab.studentski_server.model.Predmet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PredajePredmetRepositoryTest {

    @Autowired
    PredajePredmetRepository predajePredmetRepository;

    @Autowired
    TestEntityManager entityManager;

    Nastavnik nastavnik;
    Predmet predmet;

    @BeforeEach
    void pripremiNastavnikaIPredmet() {
        nastavnik = TestData.nastavnik(entityManager, "pperic@raf.rs");
        predmet = TestData.predmet(entityManager, "RS");
    }

    private PredajePredmet napraviVezu() {
        PredajePredmet predajePredmet = new PredajePredmet();
        predajePredmet.setNastavnik(nastavnik);
        predajePredmet.setPredmet(predmet);
        return predajePredmet;
    }

    @Test
    void sacuvanaVezaNastavnikPredmetSeUcitavaIzBaze() {
        Long id = predajePredmetRepository.save(napraviVezu()).getId();
        entityManager.flush();
        entityManager.clear();

        PredajePredmet ucitana = predajePredmetRepository.findById(id).orElseThrow();
        assertThat(ucitana.getNastavnik().getId()).isEqualTo(nastavnik.getId());
        assertThat(ucitana.getPredmet().getId()).isEqualTo(predmet.getId());
    }

    @Test
    void obrisanaVezaSeViseNeVidiAliOstajeUTabeli() {
        PredajePredmet predajePredmet = entityManager.persist(napraviVezu());
        predajePredmet.setObrisan(true);   // ovo radi DELETE u CRUD-u
        predajePredmetRepository.saveAndFlush(predajePredmet);
        entityManager.clear();

        assertThat(predajePredmetRepository.findByIdAndObrisanFalse(predajePredmet.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(predajePredmetRepository.findById(predajePredmet.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void nastavnikNeMozeDvaPutaDaPredajeIstiPredmet() {
        entityManager.persist(napraviVezu());

        assertThatThrownBy(() -> predajePredmetRepository.saveAndFlush(napraviVezu()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
