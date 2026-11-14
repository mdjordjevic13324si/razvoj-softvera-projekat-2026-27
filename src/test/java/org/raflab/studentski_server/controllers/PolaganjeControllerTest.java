package org.raflab.studentski_server.controllers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Stavka 4: provera spiska i preuzimanje zadatka samo u toku termina
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PolaganjeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    Nastavnik nastavnik;         // predaje RS
    Student studentNaSpisku;     // RN 12/2023, grupa A
    Student studentVanSpiska;    // RN 13/2023
    Termin termin;               // traje upravo sada
    Grupa grupaA;                // ima zadatak
    Grupa grupaB;                // nema zadatak
    byte[] zipZadatka;

    @BeforeEach
    void napraviPodatke() throws Exception {
        Predmet razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        TestPodaci.predajePredmet(entityManager, nastavnik, razvojSoftvera);

        Smer racunarskeNauke = TestPodaci.smer(entityManager, "RN");
        studentNaSpisku = TestPodaci.student(entityManager, racunarskeNauke, 12, 2023);
        studentVanSpiska = TestPodaci.student(entityManager, racunarskeNauke, 13, 2023);
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        LocalDateTime sada = LocalDateTime.now();
        termin = TestPodaci.termin(entityManager, TestPodaci.ispit(entityManager, razvojSoftvera), 1,
                sada.minusHours(1), sada.plusHours(2));
        grupaA = TestPodaci.grupa(entityManager, termin, "A");
        grupaB = TestPodaci.grupa(entityManager, termin, "B");
        TestPodaci.spisakStavka(entityManager, studentNaSpisku, grupaA, ucionica);

        // zadatak se postavlja pravim endpointom, da zip stvarno bude na disku
        zipZadatka = TestPodaci.zip("README.md", "tekst zadatka");
        mockMvc.perform(multipart("/api/zadaci")
                        .file(new MockMultipartFile("fajl", "zadatak-rs.zip", "application/zip", zipZadatka))
                        .param("grupaId", String.valueOf(grupaA.getId()))
                        .param("naslov", "Prvi zadatak")
                        .with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isOk());
    }

    private ResultActions preuzmi(Grupa grupa, Student student) throws Exception {
        return mockMvc.perform(post("/api/polaganja/" + grupa.getId() + "/preuzimanje").with(kaoStudent(student.getId())));
    }

    private void pomeriTermin(LocalDateTime pocetak, LocalDateTime kraj) {
        termin.setPocetak(pocetak);
        termin.setKraj(kraj);
        entityManager.flush();
    }

    private List<Resenje> resenjaStudenta(Student student) {
        return entityManager.createQuery("select r from Resenje r where r.student.id = :studentId", Resenje.class)
                .setParameter("studentId", student.getId())
                .getResultList();
    }

    // ---------- preuzimanje ----------

    @Test
    void studentNaSpiskuPreuzimaZadatakUTokuTermina() throws Exception {
        preuzmi(grupaA, studentNaSpisku)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("zadatak-rs.zip")))
                .andExpect(content().bytes(zipZadatka));
    }

    @Test
    void prvoPreuzimanjePraviResenjeSaVremenomPreuzimanja() throws Exception {
        LocalDateTime pre = LocalDateTime.now().minusSeconds(1);

        preuzmi(grupaA, studentNaSpisku).andExpect(status().isOk());

        List<Resenje> resenja = resenjaStudenta(studentNaSpisku);
        assertThat(resenja).hasSize(1);
        assertThat(resenja.get(0).getVremePreuzimanja()).isAfter(pre);
        assertThat(resenja.get(0).getVremePredaje()).isNull();
        assertThat(resenja.get(0).getZadatak().getGrupa().getId()).isEqualTo(grupaA.getId());
    }

    @Test
    void ponovnoPreuzimanjeNeMenjaVremePreuzimanjaINePraviDrugoResenje() throws Exception {
        preuzmi(grupaA, studentNaSpisku).andExpect(status().isOk());
        LocalDateTime prvoVreme = resenjaStudenta(studentNaSpisku).get(0).getVremePreuzimanja();

        preuzmi(grupaA, studentNaSpisku).andExpect(status().isOk());

        List<Resenje> resenja = resenjaStudenta(studentNaSpisku);
        assertThat(resenja).hasSize(1);
        assertThat(resenja.get(0).getVremePreuzimanja()).isEqualTo(prvoVreme);
    }

    @Test
    void studentKojiNijeNaSpiskuNeMozeDaPreuzmeZadatak() throws Exception {
        preuzmi(grupaA, studentVanSpiska)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("Niste na spisku")));
        assertThat(resenjaStudenta(studentVanSpiska)).isEmpty();
    }

    @Test
    void zadatakNeMozeDaSePreuzmePrePocetkaTermina() throws Exception {
        pomeriTermin(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(4));

        preuzmi(grupaA, studentNaSpisku)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("nije poceo")));
        assertThat(resenjaStudenta(studentNaSpisku)).isEmpty();
    }

    @Test
    void zadatakNeMozeDaSePreuzmePosleKrajaTermina() throws Exception {
        pomeriTermin(LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(1));

        preuzmi(grupaA, studentNaSpisku)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("zavrsen")));
    }

    @Test
    void grupaBezZadatkaVraca404() throws Exception {
        TestPodaci.spisakStavka(entityManager, studentVanSpiska, grupaB, TestPodaci.ucionica(entityManager, "RAF2"));

        preuzmi(grupaB, studentVanSpiska).andExpect(status().isNotFound());
    }

    @Test
    void nepostojecaGrupaVraca404() throws Exception {
        mockMvc.perform(post("/api/polaganja/999999/preuzimanje").with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void nastavnikNeMozeDaPreuzimaKaoStudent() throws Exception {
        mockMvc.perform(post("/api/polaganja/" + grupaA.getId() + "/preuzimanje").with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void preuzimanjeBezTokenaVraca401() throws Exception {
        mockMvc.perform(post("/api/polaganja/" + grupaA.getId() + "/preuzimanje")).andExpect(status().isUnauthorized());
    }

    // ---------- pregled polaganja ----------

    @Test
    void studentVidiSvojaPolaganja() throws Exception {
        mockMvc.perform(get("/api/polaganja").with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].grupaId").value(grupaA.getId().intValue()))
                .andExpect(jsonPath("$[0].grupaOznaka").value("A"))
                .andExpect(jsonPath("$[0].terminRedniBroj").value(1))
                .andExpect(jsonPath("$[0].predmetSifra").value("RS"))
                .andExpect(jsonPath("$[0].ucionicaOznaka").value("RAF1"))
                .andExpect(jsonPath("$[0].stanjeTermina").value("U_TOKU"))
                .andExpect(jsonPath("$[0].zadatakPostavljen").value(true))
                .andExpect(jsonPath("$[0].vremePreuzimanja").doesNotExist());
    }

    @Test
    void studentBezPolaganjaDobijaPraznuListu() throws Exception {
        mockMvc.perform(get("/api/polaganja").with(kaoStudent(studentVanSpiska.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void polaganjePokazujeVremePreuzimanjaPoslePreuzimanja() throws Exception {
        preuzmi(grupaA, studentNaSpisku).andExpect(status().isOk());

        mockMvc.perform(get("/api/polaganja/" + grupaA.getId()).with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vremePreuzimanja").isNotEmpty())
                .andExpect(jsonPath("$.vremePredaje").doesNotExist());
    }

    @Test
    void stanjeTerminaPrePocetkaIPosleKraja() throws Exception {
        pomeriTermin(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(4));
        mockMvc.perform(get("/api/polaganja/" + grupaA.getId()).with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(jsonPath("$.stanjeTermina").value("NIJE_POCEO"));

        pomeriTermin(LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(1));
        mockMvc.perform(get("/api/polaganja/" + grupaA.getId()).with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(jsonPath("$.stanjeTermina").value("ZAVRSEN"));
    }

    @Test
    void proveraSpiskaZaStudentaKojiNijeNaSpiskuVraca403SaPorukom() throws Exception {
        mockMvc.perform(get("/api/polaganja/" + grupaA.getId()).with(kaoStudent(studentVanSpiska.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("Niste na spisku")))
                .andExpect(jsonPath("$.detail", containsString("grupa A")));
    }

    @Test
    void nastavnikNeVidiPolaganjaKaoStudent() throws Exception {
        mockMvc.perform(get("/api/polaganja").with(kaoNastavnik(nastavnik.getId()))).andExpect(status().isForbidden());
    }

    // ---------- fajl zadatka za nastavnika ----------

    @Test
    void nastavnikPreuzimaFajlZadatkaBezObziraNaTermin() throws Exception {
        pomeriTermin(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(4));
        Long zadatakId = entityManager.createQuery("select z.id from Zadatak z where z.grupa.id = :grupaId", Long.class)
                .setParameter("grupaId", grupaA.getId())
                .getSingleResult();

        mockMvc.perform(get("/api/zadaci/" + zadatakId + "/fajl").with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(zipZadatka));
        mockMvc.perform(get("/api/zadaci/" + zadatakId + "/fajl").with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(status().isForbidden());
    }
}
