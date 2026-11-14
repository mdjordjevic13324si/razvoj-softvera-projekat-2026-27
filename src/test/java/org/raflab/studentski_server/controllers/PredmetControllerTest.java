package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PredmetControllerTest extends CrudControllerTestOsnova {

    @Override
    String url() {
        return "/api/predmeti";
    }

    @Override
    Long napraviUBazi() {
        return TestPodaci.predmet(entityManager, "RS").getId();
    }

    @Override
    String ispravanJson() {
        return """
                {"sifra": "OOP", "naziv": "Objektno orijentisano programiranje"}
                """;
    }

    @Override
    String neispravanJson() {
        return """
                {"sifra": "OOP", "naziv": ""}
                """;
    }

    @Test
    void odgovorSadrziPoljaPredmeta() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.sifra").value("OOP"))
                .andExpect(jsonPath("$.naziv").value("Objektno orijentisano programiranje"));
    }

    @Test
    void predmetSaPostojecomSifromVraca409() throws Exception {
        napraviUBazi();

        posalji(NASTAVNIK, """
                {"sifra": "RS", "naziv": "Razvoj softvera"}
                """).andExpect(status().isConflict());
    }
}
