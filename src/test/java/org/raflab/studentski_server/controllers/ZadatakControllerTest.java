package org.raflab.studentski_server.controllers;

import com.jayway.jsonpath.JsonPath;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ZadatakControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;
    @Value("${app.skladiste.koren}")
    String korenSkladista;

    Nastavnik nastavnik;        // predaje RS
    Nastavnik drugiNastavnik;   // ne predaje RS
    Student student;
    Grupa grupaA;

    @BeforeEach
    void napraviPodatke() {
        Predmet razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        drugiNastavnik = TestPodaci.nastavnik(entityManager, "zika@raf.rs");
        TestPodaci.predajePredmet(entityManager, nastavnik, razvojSoftvera);
        student = TestPodaci.student(entityManager, TestPodaci.smer(entityManager, "RN"), 12, 2023);

        Termin termin = TestPodaci.termin(entityManager, TestPodaci.ispit(entityManager, razvojSoftvera), 1);
        grupaA = TestPodaci.grupa(entityManager, termin, "A");
    }

    private ResultActions postaviZadatak(Long grupaId, String imeFajla, byte[] sadrzaj, RequestPostProcessor korisnik) throws Exception {
        MockMultipartFile fajl = new MockMultipartFile("fajl", imeFajla, "application/zip", sadrzaj);
        return mockMvc.perform(multipart("/api/zadaci")
                .file(fajl)
                .param("grupaId", String.valueOf(grupaId))
                .param("naslov", "Prvi zadatak")
                .param("opis", "Napraviti REST servis")
                .with(korisnik));
    }

    private ResultActions postaviIspravanZadatak() throws Exception {
        return postaviZadatak(grupaA.getId(), "zadatak.zip", TestPodaci.zip("README.md", "tekst zadatka"), kaoNastavnik(nastavnik.getId()));
    }

    private Long idIzOdgovora(ResultActions odgovor) throws Exception {
        Number id = JsonPath.read(odgovor.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    // ---------- postavljanje ----------

    @Test
    void nastavnikKojiPredajePredmetPostavljaZadatak() throws Exception {
        postaviIspravanZadatak()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.naslov").value("Prvi zadatak"))
                .andExpect(jsonPath("$.originalnoIme").value("zadatak.zip"))
                .andExpect(jsonPath("$.grupaOznaka").value("A"))
                .andExpect(jsonPath("$.terminRedniBroj").value(1))
                .andExpect(jsonPath("$.predmetSifra").value("RS"))
                .andExpect(jsonPath("$.ispitniRokNaziv").value("Januar"))
                .andExpect(jsonPath("$.skolskaGodina").value("2026-2027"))
                .andExpect(jsonPath("$.nastavnikId").value(nastavnik.getId().intValue()))
                .andExpect(jsonPath("$.putanjaZipa").doesNotExist());
    }

    @Test
    void zipSeCuvaNaDiskuPoStrukturiRokPredmetTerminGrupa() throws Exception {
        byte[] sadrzaj = TestPodaci.zip("README.md", "tekst zadatka");

        postaviZadatak(grupaA.getId(), "zadatak.zip", sadrzaj, kaoNastavnik(nastavnik.getId())).andExpect(status().isOk());

        Path ocekivanaPutanja = Path.of(korenSkladista, "2026-2027", "januar", "RS", "zadaci", "termin-1", "grupa-A.zip");
        assertThat(ocekivanaPutanja).exists();
        assertThat(Files.readAllBytes(ocekivanaPutanja)).isEqualTo(sadrzaj);
    }

    @Test
    void nastavnikKojiNePredajePredmetNeMozeDaPostaviZadatak() throws Exception {
        postaviZadatak(grupaA.getId(), "zadatak.zip", TestPodaci.zip("README.md", "x"), kaoNastavnik(drugiNastavnik.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNeMozeDaPostaviZadatak() throws Exception {
        postaviZadatak(grupaA.getId(), "zadatak.zip", TestPodaci.zip("README.md", "x"), kaoStudent(student.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void postavljanjeBezTokenaVraca401() throws Exception {
        MockMultipartFile fajl = new MockMultipartFile("fajl", "zadatak.zip", "application/zip", TestPodaci.zip("README.md", "x"));
        mockMvc.perform(multipart("/api/zadaci").file(fajl)
                        .param("grupaId", String.valueOf(grupaA.getId()))
                        .param("naslov", "Prvi zadatak"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fajlKojiNijeZipVraca400() throws Exception {
        postaviZadatak(grupaA.getId(), "zadatak.txt", "obican tekst".getBytes(), kaoNastavnik(nastavnik.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fajlSaZipImenomAliDrugimSadrzajemVraca400() throws Exception {
        postaviZadatak(grupaA.getId(), "zadatak.zip", "obican tekst".getBytes(), kaoNastavnik(nastavnik.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nepostojecaGrupaVraca404() throws Exception {
        postaviZadatak(999_999L, "zadatak.zip", TestPodaci.zip("README.md", "x"), kaoNastavnik(nastavnik.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void drugiZadatakZaIstuGrupuVraca409() throws Exception {
        postaviIspravanZadatak().andExpect(status().isOk());

        postaviIspravanZadatak().andExpect(status().isConflict());
    }

    // ---------- citanje ----------

    @Test
    void nastavnikVidiZadatak() throws Exception {
        Long zadatakId = idIzOdgovora(postaviIspravanZadatak());

        mockMvc.perform(get("/api/zadaci/" + zadatakId).with(kaoNastavnik(drugiNastavnik.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(zadatakId.intValue()))
                .andExpect(jsonPath("$.naslov").value("Prvi zadatak"));
    }

    @Test
    void studentNeVidiZadatak() throws Exception {
        Long zadatakId = idIzOdgovora(postaviIspravanZadatak());

        mockMvc.perform(get("/api/zadaci/" + zadatakId).with(kaoStudent(student.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void nepostojeciZadatakVraca404() throws Exception {
        mockMvc.perform(get("/api/zadaci/999999").with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isNotFound());
    }

    // ---------- brisanje ----------

    @Test
    void obrisanZadatakViseNePostojiAGrupaMozeDaDobijeNov() throws Exception {
        Long zadatakId = idIzOdgovora(postaviIspravanZadatak());

        mockMvc.perform(delete("/api/zadaci/" + zadatakId).with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/zadaci/" + zadatakId).with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isNotFound());
        postaviIspravanZadatak().andExpect(status().isOk());
    }

    @Test
    void nastavnikKojiNePredajePredmetNeMozeDaObriseZadatak() throws Exception {
        Long zadatakId = idIzOdgovora(postaviIspravanZadatak());

        mockMvc.perform(delete("/api/zadaci/" + zadatakId).with(kaoNastavnik(drugiNastavnik.getId())))
                .andExpect(status().isForbidden());
    }
}
