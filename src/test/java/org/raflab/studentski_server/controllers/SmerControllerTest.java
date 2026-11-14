package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SmerControllerTest extends CrudControllerTestOsnova {

    @Override
    String url() {
        return "/api/smerovi";
    }

    @Override
    Long napraviUBazi() {
        return TestPodaci.smer(entityManager, "RN").getId();
    }

    @Override
    String ispravanJson() {
        return """
                {"oznaka": "SI", "naziv": "Softversko inzenjerstvo", "vrsta": "OSNOVNE_AKADEMSKE"}
                """;
    }

    @Override
    String neispravanJson() {
        return """
                {"oznaka": "", "naziv": "Softversko inzenjerstvo", "vrsta": "OSNOVNE_AKADEMSKE"}
                """;
    }

    @Test
    void odgovorSadrziPoljaSmera() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.oznaka").value("SI"))
                .andExpect(jsonPath("$.naziv").value("Softversko inzenjerstvo"))
                .andExpect(jsonPath("$.vrsta").value("OSNOVNE_AKADEMSKE"));
    }

    @Test
    void smerSaPostojecomOznakomVraca409() throws Exception {
        napraviUBazi();

        posalji(NASTAVNIK, """
                {"oznaka": "RN", "naziv": "Racunarske nauke", "vrsta": "OSNOVNE_AKADEMSKE"}
                """).andExpect(status().isConflict());
    }

    @Test
    void nepoznataVrstaSmeraVraca400() throws Exception {
        posalji(NASTAVNIK, """
                {"oznaka": "SI", "naziv": "Softversko inzenjerstvo", "vrsta": "NEPOSTOJECA"}
                """).andExpect(status().isBadRequest());
    }
}
