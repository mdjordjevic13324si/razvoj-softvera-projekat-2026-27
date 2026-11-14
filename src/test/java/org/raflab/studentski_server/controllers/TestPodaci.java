package org.raflab.studentski_server.controllers;

import jakarta.persistence.EntityManager;
import org.raflab.studentski_server.model.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/*
 * Pomocne metode za testove kontrolera: pravljenje podataka u bazi i "prijavljeni korisnik" bez pravog logina.
 * Svaka metoda snima entitet i vraca ga sa dodeljenim id-jem.
 */
class TestPodaci {

    // ---------- prijavljeni korisnik ----------

    // zahtev kao da ga salje nastavnik sa datim id-jem (isti claim-ovi koje pravi AuthService)
    static RequestPostProcessor kaoNastavnik(Long nastavnikId) {
        return jwt().jwt(token -> token.claim("uloga", "NASTAVNIK").claim("nastavnikId", nastavnikId))
                .authorities(new SimpleGrantedAuthority("ROLE_NASTAVNIK"));
    }

    static RequestPostProcessor kaoStudent(Long studentId) {
        return jwt().jwt(token -> token.claim("uloga", "STUDENT").claim("studentId", studentId))
                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    // ---------- podaci ----------

    static Smer smer(EntityManager entityManager, String oznaka) {
        Smer smer = new Smer();
        smer.setOznaka(oznaka);
        smer.setNaziv("Smer " + oznaka);
        smer.setVrsta(VrstaSmera.OSNOVNE_AKADEMSKE);
        entityManager.persist(smer);
        return smer;
    }

    static Student student(EntityManager entityManager, Smer smer, int brojIndeksa, int godinaUpisa) {
        Student student = new Student();
        student.setIme("Ime" + brojIndeksa);
        student.setPrezime("Prezime" + brojIndeksa);
        student.setEmail(smer.getOznaka().toLowerCase() + brojIndeksa + godinaUpisa + "@raf.rs");
        student.setSmer(smer);
        student.setBrojIndeksa(brojIndeksa);
        student.setGodinaUpisa(godinaUpisa);
        entityManager.persist(student);
        return student;
    }

    static Nastavnik nastavnik(EntityManager entityManager, String email) {
        Nastavnik nastavnik = new Nastavnik();
        nastavnik.setIme("Pera");
        nastavnik.setPrezime("Peric");
        nastavnik.setEmail(email);
        entityManager.persist(nastavnik);
        return nastavnik;
    }

    static Predmet predmet(EntityManager entityManager, String sifra) {
        Predmet predmet = new Predmet();
        predmet.setSifra(sifra);
        predmet.setNaziv("Predmet " + sifra);
        entityManager.persist(predmet);
        return predmet;
    }

    static PredajePredmet predajePredmet(EntityManager entityManager, Nastavnik nastavnik, Predmet predmet) {
        PredajePredmet predajePredmet = new PredajePredmet();
        predajePredmet.setNastavnik(nastavnik);
        predajePredmet.setPredmet(predmet);
        entityManager.persist(predajePredmet);
        return predajePredmet;
    }

    static Ucionica ucionica(EntityManager entityManager, String oznaka) {
        Ucionica ucionica = new Ucionica();
        ucionica.setOznaka(oznaka);
        ucionica.setAdresa("Knez Mihailova 6");
        ucionica.setSprat(1);
        ucionica.setKapacitet(30);
        ucionica.setBrojRacunara(30);
        entityManager.persist(ucionica);
        return ucionica;
    }

    static IspitniRok ispitniRok(EntityManager entityManager, String skolskaGodina, String naziv) {
        IspitniRok ispitniRok = new IspitniRok();
        ispitniRok.setSkolskaGodina(skolskaGodina);
        ispitniRok.setNaziv(naziv);
        ispitniRok.setTip(TipRoka.ISPITNI_ROK);
        entityManager.persist(ispitniRok);
        return ispitniRok;
    }

    // ispit iz datog predmeta u roku "Januar" 2026-2027
    static Ispit ispit(EntityManager entityManager, Predmet predmet) {
        IspitniRok ispitniRok = new IspitniRok();
        ispitniRok.setSkolskaGodina("2026-2027");
        ispitniRok.setNaziv("Januar");
        ispitniRok.setTip(TipRoka.ISPITNI_ROK);
        entityManager.persist(ispitniRok);

        Ispit ispit = new Ispit();
        ispit.setIspitniRok(ispitniRok);
        ispit.setPredmet(predmet);
        ispit.setDatum(LocalDate.of(2027, 1, 20));
        entityManager.persist(ispit);
        return ispit;
    }

    static Termin termin(EntityManager entityManager, Ispit ispit, int redniBroj) {
        Termin termin = new Termin();
        termin.setIspit(ispit);
        termin.setRedniBroj(redniBroj);
        termin.setPocetak(LocalDateTime.of(2027, 1, 20, 10, 0));
        termin.setKraj(LocalDateTime.of(2027, 1, 20, 13, 0));
        entityManager.persist(termin);
        return termin;
    }

    // termin sa zadatim vremenom (za provere "pre pocetka", "u toku", "posle kraja")
    static Termin termin(EntityManager entityManager, Ispit ispit, int redniBroj, LocalDateTime pocetak, LocalDateTime kraj) {
        Termin termin = new Termin();
        termin.setIspit(ispit);
        termin.setRedniBroj(redniBroj);
        termin.setPocetak(pocetak);
        termin.setKraj(kraj);
        entityManager.persist(termin);
        return termin;
    }

    static SpisakStavka spisakStavka(EntityManager entityManager, Student student, Grupa grupa, Ucionica ucionica) {
        SpisakStavka stavka = new SpisakStavka();
        stavka.setStudent(student);
        stavka.setGrupa(grupa);
        stavka.setUcionica(ucionica);
        entityManager.persist(stavka);
        return stavka;
    }

    static Grupa grupa(EntityManager entityManager, Termin termin, String oznaka) {
        Grupa grupa = new Grupa();
        grupa.setTermin(termin);
        grupa.setOznaka(oznaka);
        entityManager.persist(grupa);
        return grupa;
    }

    // zadatak upisan pravo u bazu (bez zipa na disku) - za testove koji ne preuzimaju fajl
    static Zadatak zadatak(EntityManager entityManager, Grupa grupa, Nastavnik nastavnik) {
        Zadatak zadatak = new Zadatak();
        zadatak.setGrupa(grupa);
        zadatak.setNastavnik(nastavnik);
        zadatak.setNaslov("Zadatak za grupu " + grupa.getOznaka());
        zadatak.setPutanjaZipa("test/zadaci/grupa-" + grupa.getId() + ".zip");
        zadatak.setOriginalnoIme("zadatak.zip");
        entityManager.persist(zadatak);
        return zadatak;
    }

    // resenje sa zadatim vremenima; vremePredaje je null ako student jos nije predao
    static Resenje resenje(EntityManager entityManager, Student student, Zadatak zadatak,
                           LocalDateTime vremePreuzimanja, LocalDateTime vremePredaje) {
        Resenje resenje = new Resenje();
        resenje.setStudent(student);
        resenje.setZadatak(zadatak);
        resenje.setVremePreuzimanja(vremePreuzimanja);
        resenje.setVremePredaje(vremePredaje);
        if (vremePredaje != null) {
            resenje.setPutanjaZipa("test/resenja/student-" + student.getId() + ".zip");
        }
        entityManager.persist(resenje);
        return resenje;
    }

    // ---------- fajlovi ----------

    // pravi mali ispravan zip sa jednim fajlom
    static byte[] zip(String imeFajla, String sadrzaj) throws IOException {
        ByteArrayOutputStream bajtovi = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bajtovi)) {
            zip.putNextEntry(new ZipEntry(imeFajla));
            zip.write(sadrzaj.getBytes());
            zip.closeEntry();
        }
        return bajtovi.toByteArray();
    }

    // sadrzaj zipa: ime fajla u zipu -> bajtovi
    static Map<String, byte[]> raspakuj(byte[] zipBajtovi) throws IOException {
        Map<String, byte[]> fajlovi = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBajtovi))) {
            ZipEntry stavka;
            while ((stavka = zip.getNextEntry()) != null) {
                fajlovi.put(stavka.getName(), zip.readAllBytes());
            }
        }
        return fajlovi;
    }
}
