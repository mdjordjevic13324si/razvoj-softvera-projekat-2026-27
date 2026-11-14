package org.raflab.studentski_server.controllers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Zajednicki testovi za svaki entitet na CRUD kalupu: pet endpointa i pravila pristupa.
 * Test klasa entiteta nasledi ovu i kaze samo URL, kako se pravi red u bazi i kako izgleda JSON.
 * JUnit izvrsava nasledjene @Test metode za svaku podklasu posebno.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class CrudControllerTestOsnova {

    static final RequestPostProcessor NASTAVNIK = kaoNastavnik(1L);
    static final RequestPostProcessor STUDENT = kaoStudent(1L);

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    // ---------- sta podklasa mora da kaze ----------

    // npr. "/api/predmeti"
    abstract String url();

    // snima jedan entitet u bazu i vraca njegov id
    abstract Long napraviUBazi();

    // ispravan zahtev; jedinstvena polja moraju da se razlikuju od onog sto pravi napraviUBazi()
    abstract String ispravanJson();

    // zahtev koji ne prolazi validaciju (400)
    abstract String neispravanJson();

    // da li student sme GET (vecina sifarnika: da; studenti: ne)
    boolean studentSmeDaCita() {
        return true;
    }

    // ---------- pomocne metode ----------

    ResultActions posalji(RequestPostProcessor korisnik, String json) throws Exception {
        return mockMvc.perform(post(url()).with(korisnik).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions izmeni(Long id, RequestPostProcessor korisnik, String json) throws Exception {
        return mockMvc.perform(put(url() + "/" + id).with(korisnik).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // ---------- citanje ----------

    @Test
    void listaVracaEntitetePoStranicama() throws Exception {
        Long id = napraviUBazi();

        mockMvc.perform(get(url()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id.intValue()))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void jedanEntitetPoIdju() throws Exception {
        Long id = napraviUBazi();

        mockMvc.perform(get(url() + "/" + id).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.intValue()))
                .andExpect(jsonPath("$.obrisan").doesNotExist());
    }

    @Test
    void nepostojeciEntitetVraca404() throws Exception {
        mockMvc.perform(get(url() + "/999999").with(NASTAVNIK)).andExpect(status().isNotFound());
    }

    @Test
    void citanjeBezTokenaVraca401() throws Exception {
        mockMvc.perform(get(url())).andExpect(status().isUnauthorized());
    }

    @Test
    void studentCitaSamoAkoJeDozvoljeno() throws Exception {
        Long id = napraviUBazi();

        mockMvc.perform(get(url() + "/" + id).with(STUDENT))
                .andExpect(studentSmeDaCita() ? status().isOk() : status().isForbidden());
        mockMvc.perform(get(url()).with(STUDENT))
                .andExpect(studentSmeDaCita() ? status().isOk() : status().isForbidden());
    }

    // ---------- pravljenje ----------

    @Test
    void nastavnikPraviEntitet() throws Exception {
        posalji(NASTAVNIK, ispravanJson())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get(url()).with(NASTAVNIK)).andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void neispravanZahtevVraca400() throws Exception {
        posalji(NASTAVNIK, neispravanJson()).andExpect(status().isBadRequest());
    }

    @Test
    void studentNeSmeDaPravi() throws Exception {
        posalji(STUDENT, ispravanJson()).andExpect(status().isForbidden());
    }

    // ---------- izmena ----------

    @Test
    void nastavnikMenjaEntitet() throws Exception {
        Long id = napraviUBazi();

        izmeni(id, NASTAVNIK, ispravanJson())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.intValue()));
    }

    @Test
    void izmenaNepostojecegEntitetaVraca404() throws Exception {
        izmeni(999_999L, NASTAVNIK, ispravanJson()).andExpect(status().isNotFound());
    }

    @Test
    void studentNeSmeDaMenja() throws Exception {
        Long id = napraviUBazi();

        izmeni(id, STUDENT, ispravanJson()).andExpect(status().isForbidden());
    }

    // ---------- brisanje ----------

    @Test
    void obrisanEntitetViseNePostoji() throws Exception {
        Long id = napraviUBazi();

        mockMvc.perform(delete(url() + "/" + id).with(NASTAVNIK)).andExpect(status().isNoContent());

        mockMvc.perform(get(url() + "/" + id).with(NASTAVNIK)).andExpect(status().isNotFound());
        mockMvc.perform(get(url()).with(NASTAVNIK)).andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void studentNeSmeDaBrise() throws Exception {
        Long id = napraviUBazi();

        mockMvc.perform(delete(url() + "/" + id).with(STUDENT)).andExpect(status().isForbidden());
    }
}
