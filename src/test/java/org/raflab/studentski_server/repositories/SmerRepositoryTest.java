package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Smer;
import org.raflab.studentski_server.model.VrstaSmera;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SmerRepositoryTest {

    @Autowired
    SmerRepository smerRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void sacuvanSmerSeUcitavaIzBaze() {
        Smer smer = new Smer();
        smer.setOznaka("RN");
        smer.setNaziv("Racunarske nauke");
        smer.setVrsta(VrstaSmera.OSNOVNE_AKADEMSKE);
        Long id = smerRepository.save(smer).getId();
        entityManager.flush();
        entityManager.clear();

        Smer ucitan = smerRepository.findById(id).orElseThrow();
        assertThat(ucitan.getOznaka()).isEqualTo("RN");
        assertThat(ucitan.getNaziv()).isEqualTo("Racunarske nauke");
        assertThat(ucitan.getVrsta()).isEqualTo(VrstaSmera.OSNOVNE_AKADEMSKE);
    }

    @Test
    void obrisanSmerSeViseNeVidiAliOstajeUTabeli() {
        Smer smer = TestData.smer(entityManager, "RN");
        smer.setObrisan(true);   // ovo radi DELETE u CRUD-u
        smerRepository.saveAndFlush(smer);
        entityManager.clear();

        assertThat(smerRepository.findByIdAndObrisanFalse(smer.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(smerRepository.findById(smer.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void dvaSmeraNeMoguImatiIstuOznaku() {
        TestData.smer(entityManager, "RN");
        Smer duplikat = new Smer();
        duplikat.setOznaka("RN");
        duplikat.setNaziv("Duplikat");
        duplikat.setVrsta(VrstaSmera.MASTER);

        assertThatThrownBy(() -> smerRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
