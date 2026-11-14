package org.raflab.studentski_server.controllers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Stavka 6: podaci o preuzimanjima i predajama po grupi, terminu i studentu.
 *
 * Grupa A: student 1 predao posle 50 min (3000 s), student 2 predao posle 30 min (1800 s),
 *          student 3 samo preuzeo, student 4 nije ni preuzeo  -> prosek 2400 s
 * Grupa B: student 5 predao posle 10 min (600 s)              -> prosek 600 s
 * Grupa C: nema zadatak, student 6 na spisku
 * Termin:  6 na spisku, 4 preuzela, 3 predala                 -> prosek (3000 + 1800 + 600) / 3 = 1800 s
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MonitoringControllerTest {

    static final RequestPostProcessor NASTAVNIK = kaoNastavnik(1L);

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    Termin termin;
    Grupa grupaA;
    Grupa grupaB;
    Grupa grupaC;
    Student prviStudent;
    Student cetvrtiStudent;

    @BeforeEach
    void napraviPodatke() {
        Predmet razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        Nastavnik nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        Smer racunarskeNauke = TestPodaci.smer(entityManager, "RN");
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        LocalDateTime pocetak = LocalDateTime.now().minusHours(1);
        termin = TestPodaci.termin(entityManager, TestPodaci.ispit(entityManager, razvojSoftvera), 1,
                pocetak, pocetak.plusHours(3));
        grupaA = TestPodaci.grupa(entityManager, termin, "A");
        grupaB = TestPodaci.grupa(entityManager, termin, "B");
        grupaC = TestPodaci.grupa(entityManager, termin, "C");

        // prezimena su Prezime1, Prezime2... pa je redosled u odgovoru isti kao redosled brojeva indeksa
        prviStudent = TestPodaci.student(entityManager, racunarskeNauke, 1, 2023);
        Student drugiStudent = TestPodaci.student(entityManager, racunarskeNauke, 2, 2023);
        Student treciStudent = TestPodaci.student(entityManager, racunarskeNauke, 3, 2023);
        cetvrtiStudent = TestPodaci.student(entityManager, racunarskeNauke, 4, 2023);
        Student petiStudent = TestPodaci.student(entityManager, racunarskeNauke, 5, 2023);
        Student sestiStudent = TestPodaci.student(entityManager, racunarskeNauke, 6, 2023);

        TestPodaci.spisakStavka(entityManager, prviStudent, grupaA, ucionica);
        TestPodaci.spisakStavka(entityManager, drugiStudent, grupaA, ucionica);
        TestPodaci.spisakStavka(entityManager, treciStudent, grupaA, ucionica);
        TestPodaci.spisakStavka(entityManager, cetvrtiStudent, grupaA, ucionica);
        TestPodaci.spisakStavka(entityManager, petiStudent, grupaB, ucionica);
        TestPodaci.spisakStavka(entityManager, sestiStudent, grupaC, ucionica);

        Zadatak zadatakA = TestPodaci.zadatak(entityManager, grupaA, nastavnik);
        Zadatak zadatakB = TestPodaci.zadatak(entityManager, grupaB, nastavnik);
        TestPodaci.resenje(entityManager, prviStudent, zadatakA, pocetak, pocetak.plusMinutes(50));
        TestPodaci.resenje(entityManager, drugiStudent, zadatakA, pocetak.plusMinutes(5), pocetak.plusMinutes(35));
        TestPodaci.resenje(entityManager, treciStudent, zadatakA, pocetak.plusMinutes(2), null);
        TestPodaci.resenje(entityManager, petiStudent, zadatakB, pocetak, pocetak.plusMinutes(10));
    }

    // ---------- po grupi ----------

    @Test
    void monitoringGrupePokazujeZbirneBrojeveIProsek() throws Exception {
        mockMvc.perform(get("/api/monitoring/grupe/" + grupaA.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupaId").value(grupaA.getId().intValue()))
                .andExpect(jsonPath("$.grupaOznaka").value("A"))
                .andExpect(jsonPath("$.terminRedniBroj").value(1))
                .andExpect(jsonPath("$.predmetSifra").value("RS"))
                .andExpect(jsonPath("$.stanjeTermina").value("U_TOKU"))
                .andExpect(jsonPath("$.zadatakPostavljen").value(true))
                .andExpect(jsonPath("$.brojNaSpisku").value(4))
                .andExpect(jsonPath("$.brojPreuzelih").value(3))
                .andExpect(jsonPath("$.brojPredalih").value(2))
                .andExpect(jsonPath("$.prosecnoVremeIzradeSekundi").value(2400))
                .andExpect(jsonPath("$.studenti.length()").value(4));
    }

    @Test
    void monitoringGrupePokazujeStatusIVremenaSvakogStudenta() throws Exception {
        mockMvc.perform(get("/api/monitoring/grupe/" + grupaA.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                // predao
                .andExpect(jsonPath("$.studenti[0].studentId").value(prviStudent.getId().intValue()))
                .andExpect(jsonPath("$.studenti[0].indeks").value("RN 1/2023"))
                .andExpect(jsonPath("$.studenti[0].ucionicaOznaka").value("RAF1"))
                .andExpect(jsonPath("$.studenti[0].status").value("PREDAO"))
                .andExpect(jsonPath("$.studenti[0].vremePreuzimanja").isNotEmpty())
                .andExpect(jsonPath("$.studenti[0].vremePredaje").isNotEmpty())
                .andExpect(jsonPath("$.studenti[0].vremeIzradeSekundi").value(3000))
                .andExpect(jsonPath("$.studenti[1].status").value("PREDAO"))
                .andExpect(jsonPath("$.studenti[1].vremeIzradeSekundi").value(1800))
                // samo preuzeo
                .andExpect(jsonPath("$.studenti[2].status").value("PREUZEO"))
                .andExpect(jsonPath("$.studenti[2].vremePreuzimanja").isNotEmpty())
                .andExpect(jsonPath("$.studenti[2].vremePredaje").doesNotExist())
                .andExpect(jsonPath("$.studenti[2].vremeIzradeSekundi").doesNotExist())
                // nije ni preuzeo
                .andExpect(jsonPath("$.studenti[3].indeks").value("RN 4/2023"))
                .andExpect(jsonPath("$.studenti[3].status").value("NIJE_PREUZEO"))
                .andExpect(jsonPath("$.studenti[3].vremePreuzimanja").doesNotExist());
    }

    @Test
    void grupaBezZadatkaNemaPreuzimanjaNiProsek() throws Exception {
        mockMvc.perform(get("/api/monitoring/grupe/" + grupaC.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zadatakPostavljen").value(false))
                .andExpect(jsonPath("$.brojNaSpisku").value(1))
                .andExpect(jsonPath("$.brojPreuzelih").value(0))
                .andExpect(jsonPath("$.brojPredalih").value(0))
                .andExpect(jsonPath("$.prosecnoVremeIzradeSekundi").doesNotExist())
                .andExpect(jsonPath("$.studenti[0].status").value("NIJE_PREUZEO"));
    }

    @Test
    void monitoringNepostojeceGrupeVraca404() throws Exception {
        mockMvc.perform(get("/api/monitoring/grupe/999999").with(NASTAVNIK)).andExpect(status().isNotFound());
    }

    // ---------- po terminu ----------

    @Test
    void monitoringTerminaSabiraSveGrupe() throws Exception {
        mockMvc.perform(get("/api/monitoring/termini/" + termin.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.terminId").value(termin.getId().intValue()))
                .andExpect(jsonPath("$.predmetSifra").value("RS"))
                .andExpect(jsonPath("$.stanjeTermina").value("U_TOKU"))
                .andExpect(jsonPath("$.brojNaSpisku").value(6))
                .andExpect(jsonPath("$.brojPreuzelih").value(4))
                .andExpect(jsonPath("$.brojPredalih").value(3))
                .andExpect(jsonPath("$.prosecnoVremeIzradeSekundi").value(1800))
                .andExpect(jsonPath("$.grupe.length()").value(3))
                .andExpect(jsonPath("$.grupe[0].grupaOznaka").value("A"))
                .andExpect(jsonPath("$.grupe[0].prosecnoVremeIzradeSekundi").value(2400))
                .andExpect(jsonPath("$.grupe[1].grupaOznaka").value("B"))
                .andExpect(jsonPath("$.grupe[1].brojPredalih").value(1))
                .andExpect(jsonPath("$.grupe[1].prosecnoVremeIzradeSekundi").value(600))
                .andExpect(jsonPath("$.grupe[2].grupaOznaka").value("C"));
    }

    @Test
    void monitoringNepostojecegTerminaVraca404() throws Exception {
        mockMvc.perform(get("/api/monitoring/termini/999999").with(NASTAVNIK)).andExpect(status().isNotFound());
    }

    // ---------- po studentu ----------

    @Test
    void monitoringStudentaPokazujeNjegovaPolaganja() throws Exception {
        mockMvc.perform(get("/api/monitoring/studenti/" + prviStudent.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(prviStudent.getId().intValue()))
                .andExpect(jsonPath("$.indeks").value("RN 1/2023"))
                .andExpect(jsonPath("$.polaganja.length()").value(1))
                .andExpect(jsonPath("$.polaganja[0].grupaOznaka").value("A"))
                .andExpect(jsonPath("$.polaganja[0].predmetSifra").value("RS"))
                .andExpect(jsonPath("$.polaganja[0].ucionicaOznaka").value("RAF1"))
                .andExpect(jsonPath("$.polaganja[0].status").value("PREDAO"))
                .andExpect(jsonPath("$.polaganja[0].vremeIzradeSekundi").value(3000));
    }

    @Test
    void monitoringStudentaKojiNijePreuzeo() throws Exception {
        mockMvc.perform(get("/api/monitoring/studenti/" + cetvrtiStudent.getId()).with(NASTAVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.polaganja[0].status").value("NIJE_PREUZEO"))
                .andExpect(jsonPath("$.polaganja[0].vremePreuzimanja").doesNotExist());
    }

    @Test
    void monitoringNepostojecegStudentaVraca404() throws Exception {
        mockMvc.perform(get("/api/monitoring/studenti/999999").with(NASTAVNIK)).andExpect(status().isNotFound());
    }

    // ---------- pristup ----------

    @Test
    void studentNeVidiMonitoring() throws Exception {
        RequestPostProcessor student = kaoStudent(prviStudent.getId());

        mockMvc.perform(get("/api/monitoring/grupe/" + grupaA.getId()).with(student)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/monitoring/termini/" + termin.getId()).with(student)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/monitoring/studenti/" + prviStudent.getId()).with(student)).andExpect(status().isForbidden());
    }

    @Test
    void monitoringBezTokenaVraca401() throws Exception {
        mockMvc.perform(get("/api/monitoring/grupe/" + grupaA.getId())).andExpect(status().isUnauthorized());
    }
}
