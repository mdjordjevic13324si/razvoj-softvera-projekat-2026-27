package org.raflab.studentski_server.controllers;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SpisakControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    Nastavnik nastavnik;        // predaje RS
    Nastavnik drugiNastavnik;   // ne predaje RS
    Student prviStudent;        // RN 12/2023
    Student drugiStudent;       // RN 13/2023
    Grupa grupaA;
    Grupa grupaB;               // isti termin i ispit kao grupa A

    @BeforeEach
    void napraviPodatke() {
        Predmet razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        drugiNastavnik = TestPodaci.nastavnik(entityManager, "zika@raf.rs");
        TestPodaci.predajePredmet(entityManager, nastavnik, razvojSoftvera);

        Smer racunarskeNauke = TestPodaci.smer(entityManager, "RN");
        prviStudent = TestPodaci.student(entityManager, racunarskeNauke, 12, 2023);
        drugiStudent = TestPodaci.student(entityManager, racunarskeNauke, 13, 2023);
        TestPodaci.ucionica(entityManager, "RAF1");
        TestPodaci.ucionica(entityManager, "RAF2");

        Termin termin = TestPodaci.termin(entityManager, TestPodaci.ispit(entityManager, razvojSoftvera), 1);
        grupaA = TestPodaci.grupa(entityManager, termin, "A");
        grupaB = TestPodaci.grupa(entityManager, termin, "B");
    }

    private static String stavka(String smerOznaka, int brojIndeksa, int godinaUpisa, String ucionicaOznaka) {
        return """
                {"smerOznaka": "%s", "brojIndeksa": %d, "godinaUpisa": %d, "ucionicaOznaka": "%s"}
                """.formatted(smerOznaka, brojIndeksa, godinaUpisa, ucionicaOznaka);
    }

    private ResultActions unesiSpisak(Grupa grupa, RequestPostProcessor korisnik, String... stavke) throws Exception {
        String telo = "{\"stavke\": [" + String.join(",", stavke) + "]}";
        return mockMvc.perform(post("/api/grupe/" + grupa.getId() + "/spisak")
                .contentType(MediaType.APPLICATION_JSON)
                .content(telo)
                .with(korisnik));
    }

    private ResultActions procitajSpisak(Grupa grupa, RequestPostProcessor korisnik) throws Exception {
        return mockMvc.perform(get("/api/grupe/" + grupa.getId() + "/spisak").with(korisnik));
    }

    // ---------- unos ----------

    @Test
    void nastavnikUnosiSpisakStudenata() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()),
                stavka("RN", 12, 2023, "RAF1"),
                stavka("RN", 13, 2023, "RAF2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].indeks").value("RN 12/2023"))
                .andExpect(jsonPath("$[0].studentId").value(prviStudent.getId().intValue()))
                .andExpect(jsonPath("$[0].grupaOznaka").value("A"))
                .andExpect(jsonPath("$[0].ucionicaOznaka").value("RAF1"))
                .andExpect(jsonPath("$[1].indeks").value("RN 13/2023"))
                .andExpect(jsonPath("$[1].ucionicaOznaka").value("RAF2"));
    }

    @Test
    void ponovniUnosIstogStudentaMenjaUcionicuINePraviDuplikat() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF1")).andExpect(status().isOk());

        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF2")).andExpect(status().isOk());

        procitajSpisak(grupaA, kaoNastavnik(nastavnik.getId()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ucionicaOznaka").value("RAF2"));
    }

    @Test
    void studentNeMozeBitiUDveGrupeIstogIspita() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF1")).andExpect(status().isOk());

        unesiSpisak(grupaB, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF1"))
                .andExpect(status().isConflict());
    }

    @Test
    void nepostojeciStudentVraca404() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 999, 2023, "RAF1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nepostojecaUcionicaVraca404() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "NEMA"))
                .andExpect(status().isNotFound());
    }

    @Test
    void prazanSpisakVraca400() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId())).andExpect(status().isBadRequest());
    }

    @Test
    void stavkaBezUcioniceVraca400() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nastavnikKojiNePredajePredmetNeMozeDaUneseSpisak() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(drugiNastavnik.getId()), stavka("RN", 12, 2023, "RAF1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentNeMozeDaUneseSpisak() throws Exception {
        unesiSpisak(grupaA, kaoStudent(prviStudent.getId()), stavka("RN", 12, 2023, "RAF1"))
                .andExpect(status().isForbidden());
    }

    // ---------- citanje ----------

    @Test
    void nastavnikVidiSpisakGrupe() throws Exception {
        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF1")).andExpect(status().isOk());

        procitajSpisak(grupaA, kaoNastavnik(nastavnik.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ime").value(prviStudent.getIme()))
                .andExpect(jsonPath("$[0].prezime").value(prviStudent.getPrezime()));
        procitajSpisak(grupaB, kaoNastavnik(nastavnik.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void studentNeVidiSpisak() throws Exception {
        procitajSpisak(grupaA, kaoStudent(prviStudent.getId())).andExpect(status().isForbidden());
    }

    @Test
    void spisakNepostojeceGrupeVraca404() throws Exception {
        mockMvc.perform(get("/api/grupe/999999/spisak").with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isNotFound());
    }

    // ---------- brisanje ----------

    @Test
    void nastavnikSkidaStudentaSaSpiska() throws Exception {
        String odgovor = unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF1"))
                .andReturn().getResponse().getContentAsString();
        Number stavkaId = JsonPath.read(odgovor, "$[0].id");

        mockMvc.perform(delete("/api/grupe/" + grupaA.getId() + "/spisak/" + stavkaId).with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isNoContent());

        procitajSpisak(grupaA, kaoNastavnik(nastavnik.getId())).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void skinutStudentMozeDaSeVratiNaSpisak() throws Exception {
        String odgovor = unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF1"))
                .andReturn().getResponse().getContentAsString();
        Number stavkaId = JsonPath.read(odgovor, "$[0].id");
        mockMvc.perform(delete("/api/grupe/" + grupaA.getId() + "/spisak/" + stavkaId).with(kaoNastavnik(nastavnik.getId())))
                .andExpect(status().isNoContent());

        unesiSpisak(grupaA, kaoNastavnik(nastavnik.getId()), stavka("RN", 12, 2023, "RAF2")).andExpect(status().isOk());

        procitajSpisak(grupaA, kaoNastavnik(nastavnik.getId()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ucionicaOznaka").value("RAF2"));
    }
}
