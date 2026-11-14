package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Ucionica;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UcionicaRepositoryTest {

    @Autowired
    UcionicaRepository ucionicaRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void sacuvanaUcionicaSeUcitavaIzBaze() {
        Ucionica ucionica = new Ucionica();
        ucionica.setOznaka("RAF1");
        ucionica.setAdresa("Knez Mihailova 6");
        ucionica.setSprat(2);
        ucionica.setKapacitet(32);
        ucionica.setBrojRacunara(30);
        Long id = ucionicaRepository.save(ucionica).getId();
        entityManager.flush();
        entityManager.clear();   // brise kes, pa se ucionica stvarno cita iz baze

        Ucionica ucitana = ucionicaRepository.findById(id).orElseThrow();
        assertThat(ucitana.getOznaka()).isEqualTo("RAF1");
        assertThat(ucitana.getAdresa()).isEqualTo("Knez Mihailova 6");
        assertThat(ucitana.getSprat()).isEqualTo(2);
        assertThat(ucitana.getKapacitet()).isEqualTo(32);
        assertThat(ucitana.getBrojRacunara()).isEqualTo(30);
    }

    @Test
    void obrisanaUcionicaSeViseNeVidiAliOstajeUTabeli() {
        Ucionica ucionica = TestData.ucionica(entityManager, "RAF1");
        ucionica.setObrisan(true);   // ovo radi DELETE u CRUD-u
        ucionicaRepository.saveAndFlush(ucionica);
        entityManager.clear();

        assertThat(ucionicaRepository.findByIdAndObrisanFalse(ucionica.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(ucionicaRepository.findById(ucionica.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void dveUcioniceNeMoguImatiIstuOznaku() {
        TestData.ucionica(entityManager, "RAF1");
        Ucionica duplikat = new Ucionica();
        duplikat.setOznaka("RAF1");
        duplikat.setAdresa("Kolarceva 5");

        assertThatThrownBy(() -> ucionicaRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
