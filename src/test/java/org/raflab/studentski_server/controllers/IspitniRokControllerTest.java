package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IspitniRokControllerTest extends CrudControllerTestOsnova {

    @Override
    String url() {
        return "/api/ispitni-rokovi";
    }

    @Override
    Long napraviUBazi() {
        return TestPodaci.ispitniRok(entityManager, "2026-2027", "Januar").getId();
    }

    @Override
    String ispravanJson() {
        return """
                {"skolskaGodina": "2026-2027", "naziv": "Prvi kolokvijum", "tip": "KOLOKVIJUM"}
                """;
    }

    @Override
    String neispravanJson() {
        return """
                {"skolskaGodina": "2026-2027", "naziv": "Prvi kolokvijum"}
                """;
    }

    @Test
    void odgovorSadrziPoljaRoka() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.skolskaGodina").value("2026-2027"))
                .andExpect(jsonPath("$.naziv").value("Prvi kolokvijum"))
                .andExpect(jsonPath("$.tip").value("KOLOKVIJUM"));
    }

    @Test
    void istiRokUIstojGodiniVraca409() throws Exception {
        napraviUBazi();

        posalji(NASTAVNIK, """
                {"skolskaGodina": "2026-2027", "naziv": "Januar", "tip": "ISPITNI_ROK"}
                """).andExpect(status().isConflict());
    }
}
