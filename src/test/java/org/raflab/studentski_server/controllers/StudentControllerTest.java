package org.raflab.studentski_server.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudentControllerTest extends CrudControllerTestOsnova {

    Smer racunarskeNauke;

    @BeforeEach
    void napraviPodatke() {
        racunarskeNauke = TestPodaci.smer(entityManager, "RN");
    }

    @Override
    String url() {
        return "/api/studenti";
    }

    @Override
    Long napraviUBazi() {
        return TestPodaci.student(entityManager, racunarskeNauke, 12, 2023).getId();
    }

    private String studentJson(Long smerId, int brojIndeksa, String email) {
        return """
                {"ime": "Mika", "prezime": "Mikic", "email": "%s", "smerId": %d, "brojIndeksa": %d, "godinaUpisa": 2024}
                """.formatted(email, smerId, brojIndeksa);
    }

    @Override
    String ispravanJson() {
        return studentJson(racunarskeNauke.getId(), 77, "mika@raf.rs");
    }

    @Override
    String neispravanJson() {
        return studentJson(racunarskeNauke.getId(), 77, "ovo-nije-email");
    }

    // spisak svih studenata vidi samo nastavnik
    @Override
    boolean studentSmeDaCita() {
        return false;
    }

    @Test
    void odgovorSadrziSmerIIndeks() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(jsonPath("$.ime").value("Mika"))
                .andExpect(jsonPath("$.email").value("mika@raf.rs"))
                .andExpect(jsonPath("$.smerId").value(racunarskeNauke.getId().intValue()))
                .andExpect(jsonPath("$.smerOznaka").value("RN"))
                .andExpect(jsonPath("$.indeks").value("RN 77/2024"));
    }

    @Test
    void studentNaNepostojecemSmeruVraca404() throws Exception {
        posalji(NASTAVNIK, studentJson(999_999L, 77, "mika@raf.rs")).andExpect(status().isNotFound());
    }

    @Test
    void dvaStudentaSaIstimIndeksomVracaju409() throws Exception {
        posalji(NASTAVNIK, ispravanJson()).andExpect(status().isOk());

        posalji(NASTAVNIK, studentJson(racunarskeNauke.getId(), 77, "drugi@raf.rs")).andExpect(status().isConflict());
    }
}
