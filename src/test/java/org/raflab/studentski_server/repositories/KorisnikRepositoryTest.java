package org.raflab.studentski_server.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Korisnik;
import org.raflab.studentski_server.model.Nastavnik;
import org.raflab.studentski_server.model.Student;
import org.raflab.studentski_server.model.Uloga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class KorisnikRepositoryTest {

    @Autowired
    KorisnikRepository korisnikRepository;

    @Autowired
    TestEntityManager entityManager;

    private Korisnik napraviKorisnika(String email, Uloga uloga) {
        Korisnik korisnik = new Korisnik();
        korisnik.setEmail(email);
        korisnik.setLozinka("{noop}lozinka");
        korisnik.setUloga(uloga);
        return korisnik;
    }

    @Test
    void korisnikSaUlogomStudentJeVezanZaStudenta() {
        Student student = TestData.student(entityManager, TestData.smer(entityManager, "RN"), 12, 2023);
        Korisnik korisnik = napraviKorisnika(student.getEmail(), Uloga.STUDENT);
        korisnik.setStudent(student);
        Long id = korisnikRepository.save(korisnik).getId();
        entityManager.flush();
        entityManager.clear();

        Korisnik ucitan = korisnikRepository.findById(id).orElseThrow();
        assertThat(ucitan.getUloga()).isEqualTo(Uloga.STUDENT);
        assertThat(ucitan.getStudent().getId()).isEqualTo(student.getId());
        assertThat(ucitan.getNastavnik()).isNull();
    }

    @Test
    void korisnikSaUlogomNastavnikJeVezanZaNastavnika() {
        Nastavnik nastavnik = TestData.nastavnik(entityManager, "pperic@raf.rs");
        Korisnik korisnik = napraviKorisnika(nastavnik.getEmail(), Uloga.NASTAVNIK);
        korisnik.setNastavnik(nastavnik);
        Long id = korisnikRepository.save(korisnik).getId();
        entityManager.flush();
        entityManager.clear();

        Korisnik ucitan = korisnikRepository.findById(id).orElseThrow();
        assertThat(ucitan.getUloga()).isEqualTo(Uloga.NASTAVNIK);
        assertThat(ucitan.getNastavnik().getId()).isEqualTo(nastavnik.getId());
        assertThat(ucitan.getStudent()).isNull();
    }

    @Test
    void obrisanKorisnikSeViseNeVidiAliOstajeUTabeli() {
        Korisnik korisnik = entityManager.persist(napraviKorisnika("neko@raf.rs", Uloga.NASTAVNIK));
        korisnik.setObrisan(true);   // ovo radi DELETE u CRUD-u
        korisnikRepository.saveAndFlush(korisnik);
        entityManager.clear();

        assertThat(korisnikRepository.findByIdAndObrisanFalse(korisnik.getId())).isEmpty();   // CRUD ga vise ne vidi
        assertThat(korisnikRepository.findById(korisnik.getId())).isPresent();               // ali red je i dalje u tabeli
    }

    @Test
    void dvaKorisnikaNeMoguImatiIstiEmail() {
        entityManager.persist(napraviKorisnika("neko@raf.rs", Uloga.NASTAVNIK));

        assertThatThrownBy(() -> korisnikRepository.saveAndFlush(napraviKorisnika("neko@raf.rs", Uloga.STUDENT)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
