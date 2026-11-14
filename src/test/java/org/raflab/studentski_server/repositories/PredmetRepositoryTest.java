package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Predmet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PredmetRepositoryTest {

    @Autowired
    PredmetRepository predmetRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void sacuvanPredmetSeUcitavaIzBaze() {
        Predmet predmet = new Predmet();
        predmet.setSifra("RS");
        predmet.setNaziv("Razvoj softvera");
        Long id = predmetRepository.save(predmet).getId();
        entityManager.flush();
        entityManager.clear();

        Predmet ucitan = predmetRepository.findById(id).orElseThrow();
        assertThat(ucitan.getSifra()).isEqualTo("RS");
        assertThat(ucitan.getNaziv()).isEqualTo("Razvoj softvera");
    }

    @Test
    void obrisanPredmetSeViseNeVidiAliOstajeUTabeli() {
        Predmet predmet = TestData.predmet(entityManager, "RS");
        predmet.setObrisan(true);   // ovo radi DELETE u CRUD-u
        predmetRepository.saveAndFlush(predmet);
        entityManager.clear();

        assertThat(predmetRepository.findByIdAndObrisanFalse(predmet.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(predmetRepository.findById(predmet.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void dvaPredmetaNeMoguImatiIstuSifru() {
        TestData.predmet(entityManager, "RS");
        Predmet duplikat = new Predmet();
        duplikat.setSifra("RS");
        duplikat.setNaziv("Duplikat");

        assertThatThrownBy(() -> predmetRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
