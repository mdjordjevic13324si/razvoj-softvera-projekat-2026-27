package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.*;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Pomocne metode za pravljenje i snimanje test podataka.
 * Svaka metoda snima entitet (persist) i vraca ga sa dodeljenim id-jem.
 */
class TestData {

    static Smer smer(TestEntityManager entityManager, String oznaka) {
        Smer smer = new Smer();
        smer.setOznaka(oznaka);
        smer.setNaziv("Smer " + oznaka);
        smer.setVrsta(VrstaSmera.OSNOVNE_AKADEMSKE);
        return entityManager.persist(smer);
    }

    static Student student(TestEntityManager entityManager, Smer smer, int brojIndeksa, int godinaUpisa) {
        Student student = new Student();
        student.setIme("Ime" + brojIndeksa);
        student.setPrezime("Prezime" + brojIndeksa);
        student.setEmail(smer.getOznaka().toLowerCase() + brojIndeksa + godinaUpisa + "@raf.rs");
        student.setSmer(smer);
        student.setBrojIndeksa(brojIndeksa);
        student.setGodinaUpisa(godinaUpisa);
        return entityManager.persist(student);
    }

    static Nastavnik nastavnik(TestEntityManager entityManager, String email) {
        Nastavnik nastavnik = new Nastavnik();
        nastavnik.setIme("Pera");
        nastavnik.setPrezime("Peric");
        nastavnik.setEmail(email);
        return entityManager.persist(nastavnik);
    }

    static Predmet predmet(TestEntityManager entityManager, String sifra) {
        Predmet predmet = new Predmet();
        predmet.setSifra(sifra);
        predmet.setNaziv("Predmet " + sifra);
        return entityManager.persist(predmet);
    }

    static Ucionica ucionica(TestEntityManager entityManager, String oznaka) {
        Ucionica ucionica = new Ucionica();
        ucionica.setOznaka(oznaka);
        ucionica.setAdresa("Knez Mihailova 6");
        ucionica.setSprat(1);
        ucionica.setKapacitet(30);
        ucionica.setBrojRacunara(30);
        return entityManager.persist(ucionica);
    }

    static IspitniRok ispitniRok(TestEntityManager entityManager, String skolskaGodina, String naziv) {
        IspitniRok ispitniRok = new IspitniRok();
        ispitniRok.setSkolskaGodina(skolskaGodina);
        ispitniRok.setNaziv(naziv);
        ispitniRok.setTip(TipRoka.ISPITNI_ROK);
        return entityManager.persist(ispitniRok);
    }

    static Ispit ispit(TestEntityManager entityManager, IspitniRok ispitniRok, Predmet predmet) {
        Ispit ispit = new Ispit();
        ispit.setIspitniRok(ispitniRok);
        ispit.setPredmet(predmet);
        ispit.setDatum(LocalDate.of(2027, 1, 20));
        return entityManager.persist(ispit);
    }

    static Termin termin(TestEntityManager entityManager, Ispit ispit, int redniBroj) {
        Termin termin = new Termin();
        termin.setIspit(ispit);
        termin.setRedniBroj(redniBroj);
        termin.setPocetak(LocalDateTime.of(2027, 1, 20, 10, 0));
        termin.setKraj(LocalDateTime.of(2027, 1, 20, 13, 0));
        return entityManager.persist(termin);
    }

    static Grupa grupa(TestEntityManager entityManager, Termin termin, String oznaka) {
        Grupa grupa = new Grupa();
        grupa.setTermin(termin);
        grupa.setOznaka(oznaka);
        return entityManager.persist(grupa);
    }

    static Zadatak zadatak(TestEntityManager entityManager, Grupa grupa, Nastavnik nastavnik) {
        Zadatak zadatak = new Zadatak();
        zadatak.setGrupa(grupa);
        zadatak.setNastavnik(nastavnik);
        zadatak.setNaslov("Zadatak za grupu " + grupa.getOznaka());
        zadatak.setPutanjaZipa("2026-2027/januar/RS/zadaci/termin-1/grupa-" + grupa.getOznaka() + ".zip");
        zadatak.setOriginalnoIme("projekat.zip");
        return entityManager.persist(zadatak);
    }

    static Resenje resenje(TestEntityManager entityManager, Student student, Zadatak zadatak) {
        Resenje resenje = new Resenje();
        resenje.setStudent(student);
        resenje.setZadatak(zadatak);
        resenje.setVremePreuzimanja(LocalDateTime.of(2027, 1, 20, 10, 5));
        return entityManager.persist(resenje);
    }
}
