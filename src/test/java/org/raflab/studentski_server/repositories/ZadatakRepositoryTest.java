package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Grupa;
import org.raflab.studentski_server.model.Nastavnik;
import org.raflab.studentski_server.model.Zadatak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ZadatakRepositoryTest {

    @Autowired
    ZadatakRepository zadatakRepository;

    @Autowired
    TestEntityManager entityManager;

    Grupa grupa;
    Nastavnik nastavnik;

    @BeforeEach
    void pripremiGrupuINastavnika() {
        grupa = TestData.grupa(entityManager, TestData.termin(entityManager, TestData.ispit(entityManager,
                TestData.ispitniRok(entityManager, "2026-2027", "Januar"),
                TestData.predmet(entityManager, "RS")), 1), "A");
        nastavnik = TestData.nastavnik(entityManager, "pperic@raf.rs");
    }

    @Test
    void sacuvanZadatakSeUcitavaIzBaze() {
        Zadatak zadatak = new Zadatak();
        zadatak.setGrupa(grupa);
        zadatak.setNastavnik(nastavnik);
        zadatak.setNaslov("Biblioteka");
        zadatak.setOpis("Napraviti REST API za biblioteku");
        zadatak.setPutanjaZipa("2026-2027/januar/RS/zadaci/termin-1/grupa-A.zip");
        zadatak.setOriginalnoIme("biblioteka.zip");
        Long id = zadatakRepository.save(zadatak).getId();
        entityManager.flush();
        entityManager.clear();

        Zadatak ucitan = zadatakRepository.findById(id).orElseThrow();
        assertThat(ucitan.getGrupa().getId()).isEqualTo(grupa.getId());
        assertThat(ucitan.getNastavnik().getId()).isEqualTo(nastavnik.getId());
        assertThat(ucitan.getNaslov()).isEqualTo("Biblioteka");
        assertThat(ucitan.getOpis()).isEqualTo("Napraviti REST API za biblioteku");
        assertThat(ucitan.getPutanjaZipa()).isEqualTo("2026-2027/januar/RS/zadaci/termin-1/grupa-A.zip");
        assertThat(ucitan.getOriginalnoIme()).isEqualTo("biblioteka.zip");
    }

    @Test
    void obrisanZadatakSeViseNeVidiAliOstajeUTabeli() {
        Zadatak zadatak = TestData.zadatak(entityManager, grupa, nastavnik);
        zadatak.setObrisan(true);   // ovo radi DELETE u CRUD-u
        zadatakRepository.saveAndFlush(zadatak);
        entityManager.clear();

        assertThat(zadatakRepository.findByIdAndObrisanFalse(zadatak.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(zadatakRepository.findById(zadatak.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void grupaNeMozeImatiDvaZadatka() {
        TestData.zadatak(entityManager, grupa, nastavnik);
        Zadatak drugi = new Zadatak();
        drugi.setGrupa(grupa);
        drugi.setNastavnik(nastavnik);
        drugi.setNaslov("Drugi zadatak");
        drugi.setPutanjaZipa("drugi.zip");

        assertThatThrownBy(() -> zadatakRepository.saveAndFlush(drugi))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
