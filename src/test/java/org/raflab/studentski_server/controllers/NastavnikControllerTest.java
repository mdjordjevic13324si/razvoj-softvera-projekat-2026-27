package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NastavnikControllerTest extends CrudControllerTestOsnova {

    @Override
    String url() {
        return "/api/nastavnici";
    }

    @Override
    Long napraviUBazi() {
        return TestPodaci.nastavnik(entityManager, "pera@raf.rs").getId();
    }

    @Override
    String ispravanJson() {
        return """
                {"ime": "Zika", "prezime": "Zikic", "email": "zika@raf.rs"}
                """;
    }

    @Override
    String neispravanJson() {
        return """
                {"ime": "Zika", "prezime": "", "email": "zika@raf.rs"}
                """;
    }

    @Test
    void odgovorSadrziPoljaNastavnika() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.ime").value("Zika"))
                .andExpect(jsonPath("$.prezime").value("Zikic"))
                .andExpect(jsonPath("$.email").value("zika@raf.rs"));
    }

    @Test
    void nastavnikSaPostojecimEmailomVraca409() throws Exception {
        napraviUBazi();

        posalji(NASTAVNIK, """
                {"ime": "Drugi", "prezime": "Pera", "email": "pera@raf.rs"}
                """).andExpect(status().isConflict());
    }
}
