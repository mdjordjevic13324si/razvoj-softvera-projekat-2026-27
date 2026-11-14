package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IspitControllerTest extends CrudControllerTestOsnova {

    Predmet razvojSoftvera;
    Predmet objektnoProgramiranje;
    IspitniRok februar;

    @BeforeEach
    void napraviPodatke() {
        razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        objektnoProgramiranje = TestPodaci.predmet(entityManager, "OOP");
        februar = TestPodaci.ispitniRok(entityManager, "2026-2027", "Februar");
    }

    @Override
    String url() {
        return "/api/ispiti";
    }

    // RS u roku Januar
    @Override
    Long napraviUBazi() {
        return TestPodaci.ispit(entityManager, razvojSoftvera).getId();
    }

    private String ispitJson(Long ispitniRokId, Long predmetId) {
        return """
                {"ispitniRokId": %d, "predmetId": %d, "datum": "2027-02-10"}
                """.formatted(ispitniRokId, predmetId);
    }

    // OOP u roku Februar
    @Override
    String ispravanJson() {
        return ispitJson(februar.getId(), objektnoProgramiranje.getId());
    }

    @Override
    String neispravanJson() {
        return """
                {"ispitniRokId": %d, "predmetId": %d}
                """.formatted(februar.getId(), objektnoProgramiranje.getId());
    }

    @Test
    void odgovorSadrziRokPredmetIDatum() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.ispitniRokId").value(februar.getId().intValue()))
                .andExpect(jsonPath("$.ispitniRokNaziv").value("Februar"))
                .andExpect(jsonPath("$.skolskaGodina").value("2026-2027"))
                .andExpect(jsonPath("$.predmetSifra").value("OOP"))
                .andExpect(jsonPath("$.datum").value("2027-02-10"));
    }

    @Test
    void nepostojeciPredmetVraca404() throws Exception {
        posalji(NASTAVNIK, ispitJson(februar.getId(), 999_999L)).andExpect(status().isNotFound());
    }

    @Test
    void nepostojeciRokVraca404() throws Exception {
        posalji(NASTAVNIK, ispitJson(999_999L, razvojSoftvera.getId())).andExpect(status().isNotFound());
    }

    @Test
    void istiPredmetUIstomRokuVraca409() throws Exception {
        posalji(NASTAVNIK, ispravanJson()).andExpect(status().isOk());

        posalji(NASTAVNIK, ispravanJson()).andExpect(status().isConflict());
    }
}
