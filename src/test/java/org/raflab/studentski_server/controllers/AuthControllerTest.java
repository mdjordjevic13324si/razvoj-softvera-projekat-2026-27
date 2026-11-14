package org.raflab.studentski_server.controllers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.repositories.KorisnikRepository;
import org.raflab.studentski_server.repositories.NastavnikRepository;
import org.raflab.studentski_server.repositories.SmerRepository;
import org.raflab.studentski_server.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Ceo server (bez mreze) nad H2 bazom. @Transactional vraca bazu na staro posle svakog testa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    static final String LOZINKA = "lozinka123";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    KorisnikRepository korisnikRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    NastavnikRepository nastavnikRepository;
    @Autowired
    SmerRepository smerRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtDecoder jwtDecoder;

    // ---------- pomocne metode ----------

    private Korisnik napraviKorisnikaStudenta(String email) {
        Smer smer = new Smer();
        smer.setOznaka("RN");
        smer.setNaziv("Racunarske nauke");
        smer.setVrsta(VrstaSmera.OSNOVNE_AKADEMSKE);
        smer = smerRepository.save(smer);

        Student student = new Student();
        student.setIme("Mika");
        student.setPrezime("Mikic");
        student.setEmail(email);
        student.setSmer(smer);
        student.setBrojIndeksa(12);
        student.setGodinaUpisa(2023);
        student = studentRepository.save(student);

        Korisnik korisnik = new Korisnik();
        korisnik.setEmail(email);
        korisnik.setLozinka(passwordEncoder.encode(LOZINKA));
        korisnik.setUloga(Uloga.STUDENT);
        korisnik.setStudent(student);
        return korisnikRepository.save(korisnik);
    }

    private Korisnik napraviKorisnikaNastavnika(String email) {
        Nastavnik nastavnik = new Nastavnik();
        nastavnik.setIme("Pera");
        nastavnik.setPrezime("Peric");
        nastavnik.setEmail(email);
        nastavnik = nastavnikRepository.save(nastavnik);

        Korisnik korisnik = new Korisnik();
        korisnik.setEmail(email);
        korisnik.setLozinka(passwordEncoder.encode(LOZINKA));
        korisnik.setUloga(Uloga.NASTAVNIK);
        korisnik.setNastavnik(nastavnik);
        return korisnikRepository.save(korisnik);
    }

    private ResultActions posaljiLogin(String email, String lozinka) throws Exception {
        String telo = """
                {"email": "%s", "lozinka": "%s"}
                """.formatted(email, lozinka);
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(telo));
    }

    private String ulogujSeIVratiToken(String email) throws Exception {
        String odgovor = posaljiLogin(email, LOZINKA)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(odgovor, "$.token");
    }

    // ---------- login ----------

    @Test
    void ispravanEmailILozinkaVracajuTokenIUlogu() throws Exception {
        napraviKorisnikaStudenta("mika@raf.rs");

        posaljiLogin("mika@raf.rs", LOZINKA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.istice").isNotEmpty())
                .andExpect(jsonPath("$.uloga").value("STUDENT"));
    }

    @Test
    void emailSeNeRazlikujePoVelikimIMalimSlovima() throws Exception {
        napraviKorisnikaStudenta("mika@raf.rs");

        posaljiLogin("MIKA@RAF.RS", LOZINKA).andExpect(status().isOk());
    }

    @Test
    void tokenStudentaSadrziUloguKorisnikIdIStudentId() throws Exception {
        Korisnik korisnik = napraviKorisnikaStudenta("mika@raf.rs");

        Jwt jwt = jwtDecoder.decode(ulogujSeIVratiToken("mika@raf.rs"));

        assertThat(jwt.getSubject()).isEqualTo("mika@raf.rs");
        assertThat(jwt.getClaimAsString("uloga")).isEqualTo("STUDENT");
        assertThat(((Number) jwt.getClaim("korisnikId")).longValue()).isEqualTo(korisnik.getId());
        assertThat(((Number) jwt.getClaim("studentId")).longValue()).isEqualTo(korisnik.getStudent().getId());
        assertThat(jwt.hasClaim("nastavnikId")).isFalse();
    }

    @Test
    void tokenNastavnikaSadrziNastavnikId() throws Exception {
        Korisnik korisnik = napraviKorisnikaNastavnika("pera@raf.rs");

        Jwt jwt = jwtDecoder.decode(ulogujSeIVratiToken("pera@raf.rs"));

        assertThat(jwt.getClaimAsString("uloga")).isEqualTo("NASTAVNIK");
        assertThat(((Number) jwt.getClaim("nastavnikId")).longValue()).isEqualTo(korisnik.getNastavnik().getId());
        assertThat(jwt.hasClaim("studentId")).isFalse();
    }

    @Test
    void pogresnaLozinkaVraca401() throws Exception {
        napraviKorisnikaStudenta("mika@raf.rs");

        posaljiLogin("mika@raf.rs", "pogresna").andExpect(status().isUnauthorized());
    }

    @Test
    void nepostojeciEmailVraca401() throws Exception {
        posaljiLogin("nema.me@raf.rs", LOZINKA).andExpect(status().isUnauthorized());
    }

    @Test
    void obrisanKorisnikNeMozeDaSeUloguje() throws Exception {
        Korisnik korisnik = napraviKorisnikaStudenta("mika@raf.rs");
        korisnik.setObrisan(true);
        korisnikRepository.save(korisnik);

        posaljiLogin("mika@raf.rs", LOZINKA).andExpect(status().isUnauthorized());
    }

    @Test
    void prazanEmailVraca400() throws Exception {
        posaljiLogin("", LOZINKA).andExpect(status().isBadRequest());
    }

    @Test
    void praznaLozinkaVraca400() throws Exception {
        posaljiLogin("mika@raf.rs", "").andExpect(status().isBadRequest());
    }

    // ---------- pravila pristupa ----------

    @Test
    void zahtevBezTokenaNaZasticenUrlVraca401() throws Exception {
        mockMvc.perform(get("/api/bilo-sta")).andExpect(status().isUnauthorized());
    }

    @Test
    void studentNeSmeDaSaljePostNaUrlSamoZaNastavnika() throws Exception {
        napraviKorisnikaStudenta("mika@raf.rs");
        String token = ulogujSeIVratiToken("mika@raf.rs");

        // URL ne mora da postoji - security odbija zahtev pre nego sto stigne do kontrolera
        mockMvc.perform(post("/api/bilo-sta")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nastavnikProlaziSecurityNaPostZahtevu() throws Exception {
        napraviKorisnikaNastavnika("pera@raf.rs");
        String token = ulogujSeIVratiToken("pera@raf.rs");

        // prosao je security, a posto URL ne postoji odgovor je 404 (a ne 401/403)
        mockMvc.perform(post("/api/bilo-sta")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}

