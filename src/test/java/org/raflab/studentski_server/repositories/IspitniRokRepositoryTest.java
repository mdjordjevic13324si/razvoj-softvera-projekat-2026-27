package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.IspitniRok;
import org.raflab.studentski_server.model.TipRoka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class IspitniRokRepositoryTest {

    @Autowired
    IspitniRokRepository ispitniRokRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void sacuvanRokSeUcitavaIzBaze() {
        IspitniRok ispitniRok = new IspitniRok();
        ispitniRok.setSkolskaGodina("2026-2027");
        ispitniRok.setNaziv("Prvi kolokvijum");
        ispitniRok.setTip(TipRoka.KOLOKVIJUM);
        Long id = ispitniRokRepository.save(ispitniRok).getId();
        entityManager.flush();
        entityManager.clear();

        IspitniRok ucitan = ispitniRokRepository.findById(id).orElseThrow();
        assertThat(ucitan.getSkolskaGodina()).isEqualTo("2026-2027");
        assertThat(ucitan.getNaziv()).isEqualTo("Prvi kolokvijum");
        assertThat(ucitan.getTip()).isEqualTo(TipRoka.KOLOKVIJUM);
    }

    @Test
    void obrisanRokSeViseNeVidiAliOstajeUTabeli() {
        IspitniRok ispitniRok = TestData.ispitniRok(entityManager, "2026-2027", "Januar");
        ispitniRok.setObrisan(true);   // ovo radi DELETE u CRUD-u
        ispitniRokRepository.saveAndFlush(ispitniRok);
        entityManager.clear();

        assertThat(ispitniRokRepository.findByIdAndObrisanFalse(ispitniRok.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(ispitniRokRepository.findById(ispitniRok.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void istiNazivRokaUIstojSkolskojGodiniNijeDozvoljen() {
        TestData.ispitniRok(entityManager, "2026-2027", "Januar");
        IspitniRok duplikat = new IspitniRok();
        duplikat.setSkolskaGodina("2026-2027");
        duplikat.setNaziv("Januar");
        duplikat.setTip(TipRoka.ISPITNI_ROK);

        assertThatThrownBy(() -> ispitniRokRepository.saveAndFlush(duplikat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void istiNazivRokaURazlicitimGodinamaJeDozvoljen() {
        TestData.ispitniRok(entityManager, "2026-2027", "Januar");
        TestData.ispitniRok(entityManager, "2027-2028", "Januar");
        entityManager.flush();

        assertThat(ispitniRokRepository.count()).isEqualTo(2);
    }
}
