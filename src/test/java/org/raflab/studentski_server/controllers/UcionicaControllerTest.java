package org.raflab.studentski_server.controllers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.Ucionica;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Prvi entitet na CRUD kalupu - ovi testovi ujedno proveravaju i sam kalup (CrudService, CrudController).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UcionicaControllerTest {

    static final RequestPostProcessor NASTAVNIK = kaoNastavnik(1L);
    static final RequestPostProcessor STUDENT = kaoStudent(1L);

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    private static String ucionicaJson(String oznaka, int kapacitet) {
        return """
                {"oznaka": "%s", "adresa": "Knez Mihailova 6", "sprat": 2, "kapacitet": %d, "brojRacunara": 20}
                """.formatted(oznaka, kapacitet);
    }

    // ---------- citanje ----------

    @Test
    void listaVracaUcionicePoStranicama() throws Exception {
        TestPodaci.ucionica(entityManager, "RAF1");
        TestPodaci.ucionica(entityManager, "RAF2");
        TestPodaci.ucionica(entityManager, "RAF3");

        mockMvc.perform(get("/api/ucionice?page=0&size=2").with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].oznaka").value("RAF1"))
                .andExpect(jsonPath("$.content[1].oznaka").value("RAF2"))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    void studentSmeDaCitaUcionice() throws Exception {
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        mockMvc.perform(get("/api/ucionice").with(STUDENT)).andExpect(status().isOk());
        mockMvc.perform(get("/api/ucionice/" + ucionica.getId()).with(STUDENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oznaka").value("RAF1"));
    }

    @Test
    void jednaUcionicaPoIdju() throws Exception {
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        mockMvc.perform(get("/api/ucionice/" + ucionica.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ucionica.getId().intValue()))
                .andExpect(jsonPath("$.oznaka").value("RAF1"))
                .andExpect(jsonPath("$.kapacitet").value(30))
                .andExpect(jsonPath("$.obrisan").doesNotExist());
    }

    @Test
    void nepostojecaUcionicaVraca404() throws Exception {
        mockMvc.perform(get("/api/ucionice/999999").with(NASTAVNIK)).andExpect(status().isNotFound());
    }

    @Test
    void prevelikaStranicaVraca400() throws Exception {
        mockMvc.perform(get("/api/ucionice?size=1000").with(NASTAVNIK)).andExpect(status().isBadRequest());
    }

    @Test
    void citanjeBezTokenaVraca401() throws Exception {
        mockMvc.perform(get("/api/ucionice")).andExpect(status().isUnauthorized());
    }

    // ---------- pravljenje ----------

    @Test
    void nastavnikPraviUcionicu() throws Exception {
        mockMvc.perform(post("/api/ucionice").with(NASTAVNIK)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("RAF7", 25)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.oznaka").value("RAF7"))
                .andExpect(jsonPath("$.sprat").value(2))
                .andExpect(jsonPath("$.kapacitet").value(25));
    }

    @Test
    void studentNeSmeDaPraviUcionicu() throws Exception {
        mockMvc.perform(post("/api/ucionice").with(STUDENT)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("RAF7", 25)))
                .andExpect(status().isForbidden());
    }

    @Test
    void neispravniPodaciVracaju400() throws Exception {
        mockMvc.perform(post("/api/ucionice").with(NASTAVNIK)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("", 25)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/ucionice").with(NASTAVNIK)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("RAF7", 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ucionicaSaPostojecomOznakomVraca409() throws Exception {
        TestPodaci.ucionica(entityManager, "RAF1");

        mockMvc.perform(post("/api/ucionice").with(NASTAVNIK)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("RAF1", 25)))
                .andExpect(status().isConflict());
    }

    // ---------- izmena ----------

    @Test
    void nastavnikMenjaUcionicu() throws Exception {
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        mockMvc.perform(put("/api/ucionice/" + ucionica.getId()).with(NASTAVNIK)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("RAF1-nova", 40)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ucionica.getId().intValue()))
                .andExpect(jsonPath("$.oznaka").value("RAF1-nova"))
                .andExpect(jsonPath("$.kapacitet").value(40));

        mockMvc.perform(get("/api/ucionice/" + ucionica.getId()).with(NASTAVNIK))
                .andExpect(jsonPath("$.oznaka").value("RAF1-nova"));
    }

    @Test
    void izmenaNepostojeceUcioniceVraca404() throws Exception {
        mockMvc.perform(put("/api/ucionice/999999").with(NASTAVNIK)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("RAF7", 25)))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentNeSmeDaMenjaUcionicu() throws Exception {
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        mockMvc.perform(put("/api/ucionice/" + ucionica.getId()).with(STUDENT)
                        .contentType(MediaType.APPLICATION_JSON).content(ucionicaJson("RAF7", 25)))
                .andExpect(status().isForbidden());
    }

    // ---------- brisanje ----------

    @Test
    void obrisanaUcionicaNestajeIzListeIPoIdjuAliOstajeUBazi() throws Exception {
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");
        TestPodaci.ucionica(entityManager, "RAF2");

        mockMvc.perform(delete("/api/ucionice/" + ucionica.getId()).with(NASTAVNIK))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ucionice/" + ucionica.getId()).with(NASTAVNIK)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/ucionice").with(NASTAVNIK))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].oznaka").value("RAF2"));
        // soft brisanje: red je i dalje u tabeli
        assertThat(entityManager.find(Ucionica.class, ucionica.getId()).isObrisan()).isTrue();
    }

    @Test
    void brisanjeNepostojeceUcioniceVraca404() throws Exception {
        mockMvc.perform(delete("/api/ucionice/999999").with(NASTAVNIK)).andExpect(status().isNotFound());
    }

    @Test
    void studentNeSmeDaBriseUcionicu() throws Exception {
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        mockMvc.perform(delete("/api/ucionice/" + ucionica.getId()).with(STUDENT)).andExpect(status().isForbidden());
    }
}
