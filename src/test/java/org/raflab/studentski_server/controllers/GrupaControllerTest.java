package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GrupaControllerTest extends CrudControllerTestOsnova {

    Termin termin;

    @BeforeEach
    void napraviPodatke() {
        Ispit ispit = TestPodaci.ispit(entityManager, TestPodaci.predmet(entityManager, "RS"));
        termin = TestPodaci.termin(entityManager, ispit, 1);
    }

    @Override
    String url() {
        return "/api/grupe";
    }

    @Override
    Long napraviUBazi() {
        return TestPodaci.grupa(entityManager, termin, "A").getId();
    }

    private String grupaJson(Long terminId, String oznaka) {
        return """
                {"terminId": %d, "oznaka": "%s"}
                """.formatted(terminId, oznaka);
    }

    @Override
    String ispravanJson() {
        return grupaJson(termin.getId(), "B");
    }

    @Override
    String neispravanJson() {
        return grupaJson(termin.getId(), "");
    }

    @Test
    void odgovorSadrziTerminIPredmet() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.oznaka").value("B"))
                .andExpect(jsonPath("$.terminId").value(termin.getId().intValue()))
                .andExpect(jsonPath("$.terminRedniBroj").value(1))
                .andExpect(jsonPath("$.predmetSifra").value("RS"));
    }

    @Test
    void grupaUNepostojecemTerminuVraca404() throws Exception {
        posalji(NASTAVNIK, grupaJson(999_999L, "B")).andExpect(status().isNotFound());
    }

    @Test
    void dveGrupeSaIstomOznakomUIstomTerminuVracaju409() throws Exception {
        napraviUBazi();

        posalji(NASTAVNIK, grupaJson(termin.getId(), "A")).andExpect(status().isConflict());
    }
}
