package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Komentar;
import org.raflab.studentski_server.model.Nastavnik;
import org.raflab.studentski_server.model.Resenje;
import org.raflab.studentski_server.model.Zadatak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class KomentarRepositoryTest {

    @Autowired
    KomentarRepository komentarRepository;

    @Autowired
    TestEntityManager entityManager;

    Resenje resenje;
    Nastavnik nastavnik;

    @BeforeEach
    void pripremiResenjeINastavnika() {
        nastavnik = TestData.nastavnik(entityManager, "pperic@raf.rs");
        Zadatak zadatak = TestData.zadatak(entityManager,
                TestData.grupa(entityManager, TestData.termin(entityManager, TestData.ispit(entityManager,
                        TestData.ispitniRok(entityManager, "2026-2027", "Januar"),
                        TestData.predmet(entityManager, "RS")), 1), "A"),
                nastavnik);
        resenje = TestData.resenje(entityManager,
                TestData.student(entityManager, TestData.smer(entityManager, "RN"), 12, 2023), zadatak);
    }

    private Komentar napraviKomentar(String putanjaFajla, int linija, String tekst) {
        Komentar komentar = new Komentar();
        komentar.setResenje(resenje);
        komentar.setNastavnik(nastavnik);
        komentar.setPutanjaFajla(putanjaFajla);
        komentar.setLinija(linija);
        komentar.setTekst(tekst);
        komentar.setVreme(LocalDateTime.of(2027, 1, 25, 9, 0));
        return komentar;
    }

    @Test
    void sacuvanKomentarSeUcitavaIzBazeSaFajlomILinijom() {
        Long id = komentarRepository.save(
                napraviKomentar("src/main/java/Main.java", 42, "Ovde fali provera za null")).getId();
        entityManager.flush();
        entityManager.clear();

        Komentar ucitan = komentarRepository.findById(id).orElseThrow();
        assertThat(ucitan.getResenje().getId()).isEqualTo(resenje.getId());
        assertThat(ucitan.getNastavnik().getId()).isEqualTo(nastavnik.getId());
        assertThat(ucitan.getPutanjaFajla()).isEqualTo("src/main/java/Main.java");
        assertThat(ucitan.getLinija()).isEqualTo(42);
        assertThat(ucitan.getTekst()).isEqualTo("Ovde fali provera za null");
        assertThat(ucitan.getVreme()).isEqualTo(LocalDateTime.of(2027, 1, 25, 9, 0));
    }

    @Test
    void resenjeMozeImatiViseKomentaraNaIstojLiniji() {
        entityManager.persist(napraviKomentar("src/main/java/Main.java", 42, "Prvi komentar"));
        entityManager.persist(napraviKomentar("src/main/java/Main.java", 42, "Drugi komentar"));
        entityManager.flush();

        assertThat(komentarRepository.count()).isEqualTo(2);
    }

    @Test
    void obrisanKomentarSeViseNeVidiAliOstajeUTabeli() {
        Komentar komentar = entityManager.persist(napraviKomentar("src/main/java/Main.java", 1, "Tekst"));
        komentar.setObrisan(true);   // ovo radi DELETE u CRUD-u
        komentarRepository.saveAndFlush(komentar);
        entityManager.clear();

        assertThat(komentarRepository.findByIdAndObrisanFalse(komentar.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(komentarRepository.findById(komentar.getId())).isPresent();               // ali red je i dalje u tabeli
    }
}
