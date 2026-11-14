package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * KorisnikController nije na kalupu, ali ima istih pet endpointa,
 * pa koristi iste zajednicke testove (CrudControllerTestOsnova).
 */
class KorisnikControllerTest extends CrudControllerTestOsnova {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    String url() {
        return "/api/korisnici";
    }

    // postojeci nalog nastavnika pera@raf.rs
    @Override
    Long napraviUBazi() {
        Nastavnik nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        Korisnik korisnik = new Korisnik();
        korisnik.setEmail("pera@raf.rs");
        korisnik.setLozinka(passwordEncoder.encode("lozinka123"));
        korisnik.setUloga(Uloga.NASTAVNIK);
        korisnik.setNastavnik(nastavnik);
        entityManager.persist(korisnik);
        return korisnik.getId();
    }

    // novi nalog za studenta koji jos nema nalog
    @Override
    String ispravanJson() {
        Student student = TestPodaci.student(entityManager, TestPodaci.smer(entityManager, "RN"), 12, 2023);
        return """
                {"email": "mika@raf.rs", "lozinka": "pocetna123", "uloga": "STUDENT", "studentId": %d}
                """.formatted(student.getId());
    }

    @Override
    String neispravanJson() {
        return """
                {"email": "nije-email", "lozinka": "", "uloga": "STUDENT"}
                """;
    }

    // nalozi su samo za nastavnika, i za citanje
    @Override
    boolean studentSmeDaCita() {
        return false;
    }

    @Test
    void odgovorNemaLozinku() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.email").value("mika@raf.rs"))
                .andExpect(jsonPath("$.uloga").value("STUDENT"))
                .andExpect(jsonPath("$.studentId").isNumber())
                .andExpect(jsonPath("$.lozinka").doesNotExist());
    }

    @Test
    void studentSeUlogujeSaNalogomKojiJeNapravioNastavnik() throws Exception {
        posalji(NASTAVNIK, ispravanJson()).andExpect(status().isOk());

        // lozinka je heširana u bazi, a login je proverava preko matches
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "mika@raf.rs", "lozinka": "pocetna123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uloga").value("STUDENT"));
    }

    @Test
    void studentBezStudentIdVraca400() throws Exception {
        posalji(NASTAVNIK, """
                {"email": "mika@raf.rs", "lozinka": "pocetna123", "uloga": "STUDENT"}
                """).andExpect(status().isBadRequest());
    }

    @Test
    void nepostojeciStudentVraca404() throws Exception {
        posalji(NASTAVNIK, """
                {"email": "mika@raf.rs", "lozinka": "pocetna123", "uloga": "STUDENT", "studentId": 999999}
                """).andExpect(status().isNotFound());
    }

    @Test
    void postojeciEmailVraca409() throws Exception {
        napraviUBazi();
        Nastavnik drugi = TestPodaci.nastavnik(entityManager, "drugi@raf.rs");

        posalji(NASTAVNIK, """
                {"email": "pera@raf.rs", "lozinka": "pocetna123", "uloga": "NASTAVNIK", "nastavnikId": %d}
                """.formatted(drugi.getId())).andExpect(status().isConflict());
    }
}
