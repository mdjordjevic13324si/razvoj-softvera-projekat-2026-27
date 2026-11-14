package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Nastavnik;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class NastavnikRepositoryTest {

    @Autowired
    NastavnikRepository nastavnikRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void sacuvanNastavnikSeUcitavaIzBaze() {
        Nastavnik nastavnik = new Nastavnik();
        nastavnik.setIme("Bojana");
        nastavnik.setPrezime("Dimic");
        nastavnik.setEmail("bdimic@raf.rs");
        Long id = nastavnikRepository.save(nastavnik).getId();
        entityManager.flush();
        entityManager.clear();

        Nastavnik ucitan = nastavnikRepository.findById(id).orElseThrow();
        assertThat(ucitan.getIme()).isEqualTo("Bojana");
        assertThat(ucitan.getPrezime()).isEqualTo("Dimic");
        assertThat(ucitan.getEmail()).isEqualTo("bdimic@raf.rs");
    }

    @Test
    void obrisanNastavnikSeViseNeVidiAliOstajeUTabeli() {
        Nastavnik nastavnik = TestData.nastavnik(entityManager, "pperic@raf.rs");
        nastavnik.setObrisan(true);   // ovo radi DELETE u CRUD-u
        nastavnikRepository.saveAndFlush(nastavnik);
        entityManager.clear();

        assertThat(nastavnikRepository.findByIdAndObrisanFalse(nastavnik.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(nastavnikRepository.findById(nastavnik.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void dvaNastavnikaNeMoguImatiIstiEmail() {
        TestData.nastavnik(entityManager, "pperic@raf.rs");
        Nastavnik duplikat = new Nastavnik();
        duplikat.setIme("Drugi");
        duplikat.setPrezime("Peric");
        duplikat.setEmail("pperic@raf.rs");

        assertThatThrownBy(() -> nastavnikRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
