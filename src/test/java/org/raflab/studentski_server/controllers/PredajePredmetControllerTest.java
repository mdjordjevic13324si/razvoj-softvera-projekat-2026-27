package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PredajePredmetControllerTest extends CrudControllerTestOsnova {

    Nastavnik nastavnik;
    Predmet razvojSoftvera;
    Predmet objektnoProgramiranje;

    @BeforeEach
    void napraviPodatke() {
        nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        objektnoProgramiranje = TestPodaci.predmet(entityManager, "OOP");
    }

    @Override
    String url() {
        return "/api/predaje-predmet";
    }

    @Override
    Long napraviUBazi() {
        return TestPodaci.predajePredmet(entityManager, nastavnik, razvojSoftvera).getId();
    }

    private String vezaJson(Long nastavnikId, Long predmetId) {
        return """
                {"nastavnikId": %d, "predmetId": %d}
                """.formatted(nastavnikId, predmetId);
    }

    @Override
    String ispravanJson() {
        return vezaJson(nastavnik.getId(), objektnoProgramiranje.getId());
    }

    @Override
    String neispravanJson() {
        return """
                {"nastavnikId": %d}
                """.formatted(nastavnik.getId());
    }

    @Test
    void odgovorSadrziNastavnikaIPredmet() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.nastavnikId").value(nastavnik.getId().intValue()))
                .andExpect(jsonPath("$.nastavnikImeIPrezime").value("Pera Peric"))
                .andExpect(jsonPath("$.predmetId").value(objektnoProgramiranje.getId().intValue()))
                .andExpect(jsonPath("$.predmetSifra").value("OOP"));
    }

    @Test
    void nepostojeciNastavnikVraca404() throws Exception {
        posalji(NASTAVNIK, vezaJson(999_999L, razvojSoftvera.getId())).andExpect(status().isNotFound());
    }

    @Test
    void nepostojeciPredmetVraca404() throws Exception {
        posalji(NASTAVNIK, vezaJson(nastavnik.getId(), 999_999L)).andExpect(status().isNotFound());
    }

    @Test
    void istaVezaDvaPutaVraca409() throws Exception {
        napraviUBazi();

        posalji(NASTAVNIK, vezaJson(nastavnik.getId(), razvojSoftvera.getId())).andExpect(status().isConflict());
    }
}
