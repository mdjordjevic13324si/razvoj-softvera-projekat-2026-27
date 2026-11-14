package org.raflab.studentski_server.controllers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Ispit;
import org.raflab.studentski_server.model.Termin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.startsWith;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Entitet sa vezom (termin pripada ispitu) na CRUD kalupu
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TerminControllerTest {

    static final RequestPostProcessor NASTAVNIK = kaoNastavnik(1L);
    static final RequestPostProcessor STUDENT = kaoStudent(1L);

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    Ispit ispit;   // RS, Januar 2026-2027

    @BeforeEach
    void napraviPodatke() {
        ispit = TestPodaci.ispit(entityManager, TestPodaci.predmet(entityManager, "RS"));
    }

    private static String terminJson(Long ispitId, int redniBroj, String pocetak, String kraj) {
        return """
                {"ispitId": %d, "redniBroj": %d, "pocetak": "%s", "kraj": "%s"}
                """.formatted(ispitId, redniBroj, pocetak, kraj);
    }

    // ---------- citanje ----------

    @Test
    void listaVracaTermine() throws Exception {
        TestPodaci.termin(entityManager, ispit, 1);
        TestPodaci.termin(entityManager, ispit, 2);

        mockMvc.perform(get("/api/termini").with(STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].redniBroj").value(1))
                .andExpect(jsonPath("$.content[1].redniBroj").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void jedanTerminSaPodacimaOIspitu() throws Exception {
        Termin termin = TestPodaci.termin(entityManager, ispit, 1);

        mockMvc.perform(get("/api/termini/" + termin.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(termin.getId().intValue()))
                .andExpect(jsonPath("$.ispitId").value(ispit.getId().intValue()))
                .andExpect(jsonPath("$.predmetSifra").value("RS"))
                .andExpect(jsonPath("$.ispitniRokNaziv").value("Januar"))
                .andExpect(jsonPath("$.skolskaGodina").value("2026-2027"))
                .andExpect(jsonPath("$.redniBroj").value(1));
    }

    @Test
    void nepostojeciTerminVraca404() throws Exception {
        mockMvc.perform(get("/api/termini/999999").with(NASTAVNIK)).andExpect(status().isNotFound());
    }

    // ---------- pravljenje ----------

    @Test
    void nastavnikPraviTermin() throws Exception {
        mockMvc.perform(post("/api/termini").with(NASTAVNIK).contentType(MediaType.APPLICATION_JSON)
                        .content(terminJson(ispit.getId(), 1, "2027-01-20T10:00:00", "2027-01-20T13:00:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ispitId").value(ispit.getId().intValue()))
                .andExpect(jsonPath("$.predmetSifra").value("RS"))
                .andExpect(jsonPath("$.redniBroj").value(1))
                .andExpect(jsonPath("$.pocetak", startsWith("2027-01-20T10:00")))
                .andExpect(jsonPath("$.kraj", startsWith("2027-01-20T13:00")));
    }

    @Test
    void terminZaNepostojeciIspitVraca404() throws Exception {
        mockMvc.perform(post("/api/termini").with(NASTAVNIK).contentType(MediaType.APPLICATION_JSON)
                        .content(terminJson(999_999L, 1, "2027-01-20T10:00:00", "2027-01-20T13:00:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void krajPrePocetkaVraca400() throws Exception {
        mockMvc.perform(post("/api/termini").with(NASTAVNIK).contentType(MediaType.APPLICATION_JSON)
                        .content(terminJson(ispit.getId(), 1, "2027-01-20T13:00:00", "2027-01-20T10:00:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void terminBezIspitaVraca400() throws Exception {
        mockMvc.perform(post("/api/termini").with(NASTAVNIK).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"redniBroj": 1, "pocetak": "2027-01-20T10:00:00", "kraj": "2027-01-20T13:00:00"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dvaTerminaSaIstimRednimBrojemZaIstiIspitVracaju409() throws Exception {
        TestPodaci.termin(entityManager, ispit, 1);

        mockMvc.perform(post("/api/termini").with(NASTAVNIK).contentType(MediaType.APPLICATION_JSON)
                        .content(terminJson(ispit.getId(), 1, "2027-01-20T14:00:00", "2027-01-20T17:00:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void studentNeSmeDaPraviTermin() throws Exception {
        mockMvc.perform(post("/api/termini").with(STUDENT).contentType(MediaType.APPLICATION_JSON)
                        .content(terminJson(ispit.getId(), 1, "2027-01-20T10:00:00", "2027-01-20T13:00:00")))
                .andExpect(status().isForbidden());
    }

    // ---------- izmena ----------

    @Test
    void nastavnikMenjaVremeTermina() throws Exception {
        Termin termin = TestPodaci.termin(entityManager, ispit, 1);

        mockMvc.perform(put("/api/termini/" + termin.getId()).with(NASTAVNIK).contentType(MediaType.APPLICATION_JSON)
                        .content(terminJson(ispit.getId(), 1, "2027-01-21T09:00:00", "2027-01-21T12:00:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(termin.getId().intValue()))
                .andExpect(jsonPath("$.pocetak", startsWith("2027-01-21T09:00")))
                .andExpect(jsonPath("$.kraj", startsWith("2027-01-21T12:00")));
    }

    @Test
    void studentNeSmeDaMenjaTermin() throws Exception {
        Termin termin = TestPodaci.termin(entityManager, ispit, 1);

        mockMvc.perform(put("/api/termini/" + termin.getId()).with(STUDENT).contentType(MediaType.APPLICATION_JSON)
                        .content(terminJson(ispit.getId(), 1, "2027-01-21T09:00:00", "2027-01-21T12:00:00")))
                .andExpect(status().isForbidden());
    }

    // ---------- brisanje ----------

    @Test
    void obrisanTerminViseNePostoji() throws Exception {
        Termin termin = TestPodaci.termin(entityManager, ispit, 1);

        mockMvc.perform(delete("/api/termini/" + termin.getId()).with(NASTAVNIK)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/termini/" + termin.getId()).with(NASTAVNIK)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/termini").with(NASTAVNIK)).andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void studentNeSmeDaBriseTermin() throws Exception {
        Termin termin = TestPodaci.termin(entityManager, ispit, 1);

        mockMvc.perform(delete("/api/termini/" + termin.getId()).with(STUDENT)).andExpect(status().isForbidden());
    }
}
