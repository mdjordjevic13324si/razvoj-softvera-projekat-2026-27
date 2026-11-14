package org.raflab.studentski_server;

import lombok.RequiredArgsConstructor;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Puni bazu pocetnim podacima pri startu, samo ako je prazna (ddl-auto=update cuva podatke).
 * Ukljucen je samo kad je app.seed.ukljucen=true - u testovima je iskljucen.
 *
 * Nalozi za prijavu: nastavnik1@raf.rs, nastavnik2@raf.rs, student1@raf.rs ... student5@raf.rs,
 * lozinka iz app.seed.lozinka
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.ukljucen", havingValue = "true")
public class Seeder implements CommandLineRunner {

    private final SmerRepository smerRepository;
    private final NastavnikRepository nastavnikRepository;
    private final StudentRepository studentRepository;
    private final KorisnikRepository korisnikRepository;
    private final PredmetRepository predmetRepository;
    private final PredajePredmetRepository predajePredmetRepository;
    private final UcionicaRepository ucionicaRepository;
    private final IspitniRokRepository ispitniRokRepository;
    private final IspitRepository ispitRepository;
    private final TerminRepository terminRepository;
    private final GrupaRepository grupaRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    @Transactional
    public void run(String... args) {
        if (smerRepository.count() > 0) return;   // baza je vec popunjena

        String hesiranaLozinka = passwordEncoder.encode(env.getProperty("app.seed.lozinka", "lozinka123"));

        // ---------- smerovi ----------
        Smer racunarskeNauke = napraviSmer("RN", "Racunarske nauke");
        Smer racunarskoInzenjerstvo = napraviSmer("RI", "Racunarsko inzenjerstvo");
        Smer softverskoInzenjerstvo = napraviSmer("SI", "Softversko inzenjerstvo");
        List<Smer> smerovi = List.of(racunarskeNauke, racunarskoInzenjerstvo, softverskoInzenjerstvo);

        // ---------- nastavnici i njihovi nalozi ----------
        List<Nastavnik> nastavnici = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Nastavnik nastavnik = new Nastavnik();
            nastavnik.setIme("Nastavnik" + i);
            nastavnik.setPrezime("Prezime" + i);
            nastavnik.setEmail("nastavnik" + i + "@raf.rs");
            nastavnik = nastavnikRepository.save(nastavnik);
            nastavnici.add(nastavnik);

            Korisnik korisnik = new Korisnik();
            korisnik.setEmail(nastavnik.getEmail());
            korisnik.setLozinka(hesiranaLozinka);
            korisnik.setUloga(Uloga.NASTAVNIK);
            korisnik.setNastavnik(nastavnik);
            korisnikRepository.save(korisnik);
        }

        // ---------- studenti i njihovi nalozi ----------
        for (int i = 1; i <= 5; i++) {
            Student student = new Student();
            student.setIme("Student" + i);
            student.setPrezime("Prezime" + i);
            student.setEmail("student" + i + "@raf.rs");
            student.setSmer(smerovi.get((i - 1) % smerovi.size()));
            student.setBrojIndeksa(i);
            student.setGodinaUpisa(2023);
            student = studentRepository.save(student);

            Korisnik korisnik = new Korisnik();
            korisnik.setEmail(student.getEmail());
            korisnik.setLozinka(hesiranaLozinka);
            korisnik.setUloga(Uloga.STUDENT);
            korisnik.setStudent(student);
            korisnikRepository.save(korisnik);
        }

        // ---------- predmeti i ko ih predaje ----------
        Predmet razvojSoftvera = napraviPredmet("RS", "Razvoj softvera");
        Predmet objektnoProgramiranje = napraviPredmet("OOP", "Objektno orijentisano programiranje");
        napraviPredajePredmet(nastavnici.get(0), razvojSoftvera);
        napraviPredajePredmet(nastavnici.get(1), objektnoProgramiranje);

        // ---------- ucionice ----------
        napraviUcionicu("RAF1", "Knez Mihailova 6", 1, 30, 30);
        napraviUcionicu("RAF2", "Knez Mihailova 6", 2, 25, 25);

        // ---------- ispitni rok, ispit, termin i grupe (za kasnije stavke) ----------
        IspitniRok januar = new IspitniRok();
        januar.setSkolskaGodina("2026-2027");
        januar.setNaziv("Januar");
        januar.setTip(TipRoka.ISPITNI_ROK);
        januar = ispitniRokRepository.save(januar);

        Ispit ispit = new Ispit();
        ispit.setIspitniRok(januar);
        ispit.setPredmet(razvojSoftvera);
        ispit.setDatum(LocalDate.of(2027, 1, 20));
        ispit = ispitRepository.save(ispit);

        Termin termin = new Termin();
        termin.setIspit(ispit);
        termin.setRedniBroj(1);
        termin.setPocetak(LocalDateTime.of(2027, 1, 20, 10, 0));
        termin.setKraj(LocalDateTime.of(2027, 1, 20, 13, 0));
        termin = terminRepository.save(termin);

        napraviGrupu(termin, "A");
        napraviGrupu(termin, "B");
    }

    private Smer napraviSmer(String oznaka, String naziv) {
        Smer smer = new Smer();
        smer.setOznaka(oznaka);
        smer.setNaziv(naziv);
        smer.setVrsta(VrstaSmera.OSNOVNE_AKADEMSKE);
        return smerRepository.save(smer);
    }

    private Predmet napraviPredmet(String sifra, String naziv) {
        Predmet predmet = new Predmet();
        predmet.setSifra(sifra);
        predmet.setNaziv(naziv);
        return predmetRepository.save(predmet);
    }

    private void napraviPredajePredmet(Nastavnik nastavnik, Predmet predmet) {
        PredajePredmet predajePredmet = new PredajePredmet();
        predajePredmet.setNastavnik(nastavnik);
        predajePredmet.setPredmet(predmet);
        predajePredmetRepository.save(predajePredmet);
    }

    private void napraviUcionicu(String oznaka, String adresa, int sprat, int kapacitet, int brojRacunara) {
        Ucionica ucionica = new Ucionica();
        ucionica.setOznaka(oznaka);
        ucionica.setAdresa(adresa);
        ucionica.setSprat(sprat);
        ucionica.setKapacitet(kapacitet);
        ucionica.setBrojRacunara(brojRacunara);
        ucionicaRepository.save(ucionica);
    }

    private void napraviGrupu(Termin termin, String oznaka) {
        Grupa grupa = new Grupa();
        grupa.setTermin(termin);
        grupa.setOznaka(oznaka);
        grupaRepository.save(grupa);
    }
}

