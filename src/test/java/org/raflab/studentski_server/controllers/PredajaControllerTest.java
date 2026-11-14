package org.raflab.studentski_server.controllers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Stavka 5: predaja resenja uz evidenciju vremena, moguca samo do kraja termina
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PredajaControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;
    @Value("${app.skladiste.koren}")
    String korenSkladista;

    Nastavnik nastavnik;         // predaje RS
    Student studentNaSpisku;     // RN 12/2023, grupa A
    Student studentVanSpiska;    // RN 13/2023
    Termin termin;               // traje upravo sada
    Grupa grupaA;                // ima zadatak

    @BeforeEach
    void napraviPodatke() throws Exception {
        Predmet razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        TestPodaci.predajePredmet(entityManager, nastavnik, razvojSoftvera);

        Smer racunarskeNauke = TestPodaci.smer(entityManager, "RN");
        studentNaSpisku = TestPodaci.student(entityManager, racunarskeNauke, 12, 2023);
        studentVanSpiska = TestPodaci.student(entityManager, racunarskeNauke, 13, 2023);

        LocalDateTime sada = LocalDateTime.now();
        termin = TestPodaci.termin(entityManager, TestPodaci.ispit(entityManager, razvojSoftvera), 1,
                sada.minusHours(1), sada.plusHours(2));
        grupaA = TestPodaci.grupa(entityManager, termin, "A");
        TestPodaci.spisakStavka(entityManager, studentNaSpisku, grupaA, TestPodaci.ucionica(entityManager, "RAF1"));

        mockMvc.perform(multipart("/api/zadaci")
                        .file(new MockMultipartFile("fajl", "zadatak.zip", "application/zip", TestPodaci.zip("README.md", "tekst zadatka")))
                        .param("grupaId", String.valueOf(grupaA.getId()))
                        .param("naslov", "Prvi zadatak")
                        .with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isOk());
    }

    private void preuzmiZadatak(Student student) throws Exception {
        mockMvc.perform(post("/api/polaganja/" + grupaA.getId() + "/preuzimanje").with(kaoStudent(student.getId())))
                .andExpect(status().isOk());
    }

    private ResultActions predaj(String imeFajla, byte[] sadrzaj, RequestPostProcessor korisnik) throws Exception {
        return mockMvc.perform(multipart("/api/polaganja/" + grupaA.getId() + "/predaja")
                .file(new MockMultipartFile("fajl", imeFajla, "application/zip", sadrzaj))
                .with(korisnik));
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

    private Path putanjaResenjaNaDisku() {
        return Path.of(korenSkladista, "2026-2027", "januar", "RS", "resenja", "termin-1", "grupa-A", "RN-12-2023.zip");
    }

    // ---------- predaja ----------

    @Test
    void studentPredajeResenjeIDobijaPotvrdu() throws Exception {
        preuzmiZadatak(studentNaSpisku);
        byte[] resenje = TestPodaci.zip("Main.java", "class Main {}");

        predaj("moj-rad.zip", resenje, kaoStudent(studentNaSpisku.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resenjeId").isNumber())
                .andExpect(jsonPath("$.indeks").value("RN 12/2023"))
                .andExpect(jsonPath("$.predmetSifra").value("RS"))
                .andExpect(jsonPath("$.terminRedniBroj").value(1))
                .andExpect(jsonPath("$.grupaOznaka").value("A"))
                .andExpect(jsonPath("$.vremePreuzimanja").isNotEmpty())
                .andExpect(jsonPath("$.vremePredaje").isNotEmpty())
                .andExpect(jsonPath("$.velicinaBajtova").value(resenje.length));
    }

    @Test
    void predajaBeleziVremeICuvaZipNaDisku() throws Exception {
        preuzmiZadatak(studentNaSpisku);
        byte[] resenje = TestPodaci.zip("Main.java", "class Main {}");
        LocalDateTime pre = LocalDateTime.now().minusSeconds(1);

        predaj("moj-rad.zip", resenje, kaoStudent(studentNaSpisku.getId())).andExpect(status().isOk());

        List<Resenje> resenja = resenjaStudenta(studentNaSpisku);
        assertThat(resenja).hasSize(1);
        assertThat(resenja.get(0).getVremePredaje()).isAfter(pre);
        assertThat(resenja.get(0).getVremePredaje()).isAfterOrEqualTo(resenja.get(0).getVremePreuzimanja());
        assertThat(resenja.get(0).getPutanjaZipa()).isEqualTo("2026-2027/januar/RS/resenja/termin-1/grupa-A/RN-12-2023.zip");
        assertThat(putanjaResenjaNaDisku()).exists();
        assertThat(Files.readAllBytes(putanjaResenjaNaDisku())).isEqualTo(resenje);
    }

    @Test
    void ponovnaPredajaZamenjujeZipIVremeANePraviDrugoResenje() throws Exception {
        preuzmiZadatak(studentNaSpisku);
        predaj("prva.zip", TestPodaci.zip("Main.java", "prva verzija"), kaoStudent(studentNaSpisku.getId()))
                .andExpect(status().isOk());
        LocalDateTime vremePreuzimanja = resenjaStudenta(studentNaSpisku).get(0).getVremePreuzimanja();
        byte[] drugaVerzija = TestPodaci.zip("Main.java", "druga, duza verzija resenja");

        predaj("druga.zip", drugaVerzija, kaoStudent(studentNaSpisku.getId())).andExpect(status().isOk());

        List<Resenje> resenja = resenjaStudenta(studentNaSpisku);
        assertThat(resenja).hasSize(1);
        assertThat(resenja.get(0).getVremePreuzimanja()).isEqualTo(vremePreuzimanja);
        assertThat(Files.readAllBytes(putanjaResenjaNaDisku())).isEqualTo(drugaVerzija);
    }

    @Test
    void predajaPosleKrajaTerminaNijeMoguca() throws Exception {
        preuzmiZadatak(studentNaSpisku);
        pomeriTermin(LocalDateTime.now().minusHours(4), LocalDateTime.now().minusMinutes(1));

        predaj("moj-rad.zip", TestPodaci.zip("Main.java", "kasno"), kaoStudent(studentNaSpisku.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("zavrsen")));

        assertThat(resenjaStudenta(studentNaSpisku).get(0).getVremePredaje()).isNull();
    }

    @Test
    void predajaBezPreuzetogZadatkaVraca409() throws Exception {
        predaj("moj-rad.zip", TestPodaci.zip("Main.java", "x"), kaoStudent(studentNaSpisku.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void fajlKojiNijeZipVraca400() throws Exception {
        preuzmiZadatak(studentNaSpisku);

        predaj("moj-rad.txt", "obican tekst".getBytes(), kaoStudent(studentNaSpisku.getId()))
                .andExpect(status().isBadRequest());
        assertThat(resenjaStudenta(studentNaSpisku).get(0).getVremePredaje()).isNull();
    }

    @Test
    void studentKojiNijeNaSpiskuNeMozeDaPreda() throws Exception {
        predaj("moj-rad.zip", TestPodaci.zip("Main.java", "x"), kaoStudent(studentVanSpiska.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("Niste na spisku")));
    }

    @Test
    void nastavnikNeMozeDaPredajeResenje() throws Exception {
        predaj("moj-rad.zip", TestPodaci.zip("Main.java", "x"), kaoNastavnik(nastavnik.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void predajaBezTokenaVraca401() throws Exception {
        mockMvc.perform(multipart("/api/polaganja/" + grupaA.getId() + "/predaja")
                        .file(new MockMultipartFile("fajl", "moj-rad.zip", "application/zip", TestPodaci.zip("Main.java", "x"))))
                .andExpect(status().isUnauthorized());
    }

    // ---------- posle predaje ----------

    @Test
    void polaganjePokazujeVremePredaje() throws Exception {
        preuzmiZadatak(studentNaSpisku);
        predaj("moj-rad.zip", TestPodaci.zip("Main.java", "x"), kaoStudent(studentNaSpisku.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/api/polaganja/" + grupaA.getId()).with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vremePreuzimanja").isNotEmpty())
                .andExpect(jsonPath("$.vremePredaje").isNotEmpty());
    }

    @Test
    void studentPreuzimaSvojePredatoResenje() throws Exception {
        preuzmiZadatak(studentNaSpisku);
        byte[] resenje = TestPodaci.zip("Main.java", "class Main {}");
        predaj("moj-rad.zip", resenje, kaoStudent(studentNaSpisku.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/api/polaganja/" + grupaA.getId() + "/resenje").with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(resenje));
    }

    @Test
    void resenjeKojeNijePredatoVraca404() throws Exception {
        preuzmiZadatak(studentNaSpisku);

        mockMvc.perform(get("/api/polaganja/" + grupaA.getId() + "/resenje").with(kaoStudent(studentNaSpisku.getId())))
                .andExpect(status().isNotFound());
    }
}
