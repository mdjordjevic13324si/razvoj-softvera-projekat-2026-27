package org.raflab.studentski_server.controllers;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studentski_server.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoNastavnik;
import static org.raflab.studentski_server.controllers.TestPodaci.kaoStudent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Stavka 7: preuzimanje resenja po kriterijumima, poeni, komentari, i student vidi samo svoje.
 *
 * Predmet RS, jedan ispit, jedan termin koji traje upravo sada:
 *   grupa A: student 1 predao, student 2 predao, student 4 samo preuzeo
 *   grupa B: student 3 predao
 * Sve ide kroz prave endpointe (postavljanje, preuzimanje, predaja), da zipovi stvarno budu na disku.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OcenjivanjeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager entityManager;

    Nastavnik nastavnik;         // predaje RS
    Nastavnik drugiNastavnik;    // predaje samo OOP
    Predmet razvojSoftvera;
    Ispit ispit;
    Termin termin;
    Grupa grupaA;
    Grupa grupaB;
    Student prviStudent;
    Student drugiStudent;
    Student treciStudent;
    Student cetvrtiStudent;
    byte[] zipPrvog;
    byte[] zipDrugog;
    Long resenjePrvog;
    Long resenjeDrugog;
    Long resenjeCetvrtog;        // preuzeto, nije predato

    @BeforeEach
    void napraviPodatke() throws Exception {
        razvojSoftvera = TestPodaci.predmet(entityManager, "RS");
        Predmet objektnoProgramiranje = TestPodaci.predmet(entityManager, "OOP");
        nastavnik = TestPodaci.nastavnik(entityManager, "pera@raf.rs");
        drugiNastavnik = TestPodaci.nastavnik(entityManager, "zika@raf.rs");
        TestPodaci.predajePredmet(entityManager, nastavnik, razvojSoftvera);
        TestPodaci.predajePredmet(entityManager, drugiNastavnik, objektnoProgramiranje);

        Smer racunarskeNauke = TestPodaci.smer(entityManager, "RN");
        prviStudent = TestPodaci.student(entityManager, racunarskeNauke, 1, 2023);
        drugiStudent = TestPodaci.student(entityManager, racunarskeNauke, 2, 2023);
        treciStudent = TestPodaci.student(entityManager, racunarskeNauke, 3, 2023);
        cetvrtiStudent = TestPodaci.student(entityManager, racunarskeNauke, 4, 2023);
        Ucionica ucionica = TestPodaci.ucionica(entityManager, "RAF1");

        LocalDateTime sada = LocalDateTime.now();
        ispit = TestPodaci.ispit(entityManager, razvojSoftvera);
        termin = TestPodaci.termin(entityManager, ispit, 1, sada.minusHours(1), sada.plusHours(2));
        grupaA = TestPodaci.grupa(entityManager, termin, "A");
        grupaB = TestPodaci.grupa(entityManager, termin, "B");
        TestPodaci.spisakStavka(entityManager, prviStudent, grupaA, ucionica);
        TestPodaci.spisakStavka(entityManager, drugiStudent, grupaA, ucionica);
        TestPodaci.spisakStavka(entityManager, cetvrtiStudent, grupaA, ucionica);
        TestPodaci.spisakStavka(entityManager, treciStudent, grupaB, ucionica);

        postaviZadatak(grupaA);
        postaviZadatak(grupaB);

        zipPrvog = TestPodaci.zip("Main.java", "resenje prvog studenta");
        zipDrugog = TestPodaci.zip("Main.java", "resenje drugog studenta, malo duze");
        preuzmiIPredaj(grupaA, prviStudent, zipPrvog);
        preuzmiIPredaj(grupaA, drugiStudent, zipDrugog);
        preuzmiIPredaj(grupaB, treciStudent, TestPodaci.zip("Main.java", "resenje treceg studenta"));
        preuzmi(grupaA, cetvrtiStudent);

        resenjePrvog = idResenja(prviStudent);
        resenjeDrugog = idResenja(drugiStudent);
        resenjeCetvrtog = idResenja(cetvrtiStudent);
    }

    // ---------- pomocne metode ----------

    private RequestPostProcessor nastavnikRS() {
        return kaoNastavnik(nastavnik.getId());
    }

    private void postaviZadatak(Grupa grupa) throws Exception {
        mockMvc.perform(multipart("/api/zadaci")
                        .file(new MockMultipartFile("fajl", "zadatak.zip", "application/zip", TestPodaci.zip("README.md", "tekst")))
                        .param("grupaId", String.valueOf(grupa.getId()))
                        .param("naslov", "Zadatak " + grupa.getOznaka())
                        .with(nastavnikRS()))
                .andExpect(status().isOk());
    }

    private void preuzmi(Grupa grupa, Student student) throws Exception {
        mockMvc.perform(post("/api/polaganja/" + grupa.getId() + "/preuzimanje").with(kaoStudent(student.getId())))
                .andExpect(status().isOk());
    }

    private void preuzmiIPredaj(Grupa grupa, Student student, byte[] zip) throws Exception {
        preuzmi(grupa, student);
        mockMvc.perform(multipart("/api/polaganja/" + grupa.getId() + "/predaja")
                        .file(new MockMultipartFile("fajl", "rad.zip", "application/zip", zip))
                        .with(kaoStudent(student.getId())))
                .andExpect(status().isOk());
    }

    private Long idResenja(Student student) {
        return entityManager.createQuery("select r.id from Resenje r where r.student.id = :studentId", Long.class)
                .setParameter("studentId", student.getId())
                .getSingleResult();
    }

    private ResultActions upisiPoene(Long resenjeId, int poeni, RequestPostProcessor korisnik) throws Exception {
        return mockMvc.perform(put("/api/resenja/" + resenjeId + "/poeni").with(korisnik)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"poeni\": " + poeni + "}"));
    }

    private static String komentarJson(String putanjaFajla, int linija, String tekst) {
        return """
                {"putanjaFajla": "%s", "linija": %d, "tekst": "%s"}
                """.formatted(putanjaFajla, linija, tekst);
    }

    private ResultActions dodajKomentar(Long resenjeId, String json, RequestPostProcessor korisnik) throws Exception {
        return mockMvc.perform(post("/api/resenja/" + resenjeId + "/komentari").with(korisnik)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private Long idKomentara(ResultActions odgovor) throws Exception {
        Number id = JsonPath.read(odgovor.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    // ---------- pretraga resenja ----------

    @Test
    void resenjaPoGrupiSuSamoPredataResenjaTeGrupe() throws Exception {
        mockMvc.perform(get("/api/resenja?grupaId=" + grupaA.getId()).with(nastavnikRS()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(resenjePrvog.intValue()))
                .andExpect(jsonPath("$[0].indeks").value("RN 1/2023"))
                .andExpect(jsonPath("$[0].grupaOznaka").value("A"))
                .andExpect(jsonPath("$[0].predmetSifra").value("RS"))
                .andExpect(jsonPath("$[0].predato").value(true))
                .andExpect(jsonPath("$[0].vremePredaje").isNotEmpty())
                .andExpect(jsonPath("$[0].poeni").doesNotExist())
                .andExpect(jsonPath("$[0].komentari.length()").value(0))
                .andExpect(jsonPath("$[1].indeks").value("RN 2/2023"));
    }

    @Test
    void resenjaPoTerminuPredmetuIRoku() throws Exception {
        mockMvc.perform(get("/api/resenja?terminId=" + termin.getId()).with(nastavnikRS()))
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/resenja?predmetId=" + razvojSoftvera.getId()).with(nastavnikRS()))
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/resenja?ispitniRokId=" + ispit.getIspitniRok().getId()).with(nastavnikRS()))
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/resenja").with(nastavnikRS()))
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/resenja?predmetId=" + razvojSoftvera.getId() + "&grupaId=" + grupaB.getId()).with(nastavnikRS()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].indeks").value("RN 3/2023"));
        mockMvc.perform(get("/api/resenja?terminId=999999").with(nastavnikRS()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void nastavnikKojiNePredajePredmetNeVidiNjegovaResenja() throws Exception {
        mockMvc.perform(get("/api/resenja").with(kaoNastavnik(drugiNastavnik.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- preuzimanje resenja ----------

    @Test
    void nastavnikPreuzimaJednoResenje() throws Exception {
        mockMvc.perform(get("/api/resenja/" + resenjePrvog + "/fajl").with(nastavnikRS()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(zipPrvog));
    }

    @Test
    void nastavnikPreuzimaSvaResenjaGrupeUJednomZipu() throws Exception {
        byte[] odgovor = mockMvc.perform(get("/api/resenja/zip?grupaId=" + grupaA.getId()).with(nastavnikRS()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andReturn().getResponse().getContentAsByteArray();

        Map<String, byte[]> fajlovi = TestPodaci.raspakuj(odgovor);
        assertThat(fajlovi).hasSize(2);
        assertThat(fajlovi.get("2026-2027/januar/RS/resenja/termin-1/grupa-A/RN-1-2023.zip")).isEqualTo(zipPrvog);
        assertThat(fajlovi.get("2026-2027/januar/RS/resenja/termin-1/grupa-A/RN-2-2023.zip")).isEqualTo(zipDrugog);
    }

    @Test
    void zipZaCeoTerminSadrziResenjaSvihGrupa() throws Exception {
        byte[] odgovor = mockMvc.perform(get("/api/resenja/zip?terminId=" + termin.getId()).with(nastavnikRS()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(TestPodaci.raspakuj(odgovor).keySet()).containsExactlyInAnyOrder(
                "2026-2027/januar/RS/resenja/termin-1/grupa-A/RN-1-2023.zip",
                "2026-2027/januar/RS/resenja/termin-1/grupa-A/RN-2-2023.zip",
                "2026-2027/januar/RS/resenja/termin-1/grupa-B/RN-3-2023.zip");
    }

    @Test
    void zipBezIjednogResenjaVraca404() throws Exception {
        mockMvc.perform(get("/api/resenja/zip?terminId=999999").with(nastavnikRS())).andExpect(status().isNotFound());
    }

    @Test
    void fajlNepredatogResenjaVraca404() throws Exception {
        mockMvc.perform(get("/api/resenja/" + resenjeCetvrtog + "/fajl").with(nastavnikRS())).andExpect(status().isNotFound());
    }

    @Test
    void nastavnikKojiNePredajePredmetNeMozeDaPreuzmeResenje() throws Exception {
        mockMvc.perform(get("/api/resenja/" + resenjePrvog + "/fajl").with(kaoNastavnik(drugiNastavnik.getId())))
                .andExpect(status().isForbidden());
    }

    // ---------- poeni ----------

    @Test
    void nastavnikUpisujePoene() throws Exception {
        upisiPoene(resenjePrvog, 85, nastavnikRS())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resenjePrvog.intValue()))
                .andExpect(jsonPath("$.poeni").value(85));

        mockMvc.perform(get("/api/resenja/" + resenjePrvog).with(nastavnikRS()))
                .andExpect(jsonPath("$.poeni").value(85));
    }

    @Test
    void poeniVanOpsegaVracaju400() throws Exception {
        upisiPoene(resenjePrvog, 101, nastavnikRS()).andExpect(status().isBadRequest());
        upisiPoene(resenjePrvog, -1, nastavnikRS()).andExpect(status().isBadRequest());
    }

    @Test
    void poeneNeMozeDaUpiseNastavnikKojiNePredajeNiStudent() throws Exception {
        upisiPoene(resenjePrvog, 85, kaoNastavnik(drugiNastavnik.getId())).andExpect(status().isForbidden());
        upisiPoene(resenjePrvog, 100, kaoStudent(prviStudent.getId())).andExpect(status().isForbidden());
    }

    @Test
    void poeniZaNepostojeceResenjeVracaju404() throws Exception {
        upisiPoene(999_999L, 85, nastavnikRS()).andExpect(status().isNotFound());
    }

    // ---------- komentari ----------

    @Test
    void nastavnikDodajeKomentarNaLinijuFajla() throws Exception {
        dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 12, "Ovde fali provera za null"), nastavnikRS())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.putanjaFajla").value("src/Main.java"))
                .andExpect(jsonPath("$.linija").value(12))
                .andExpect(jsonPath("$.tekst").value("Ovde fali provera za null"))
                .andExpect(jsonPath("$.vreme").isNotEmpty())
                .andExpect(jsonPath("$.nastavnikId").value(nastavnik.getId().intValue()))
                .andExpect(jsonPath("$.nastavnikImeIPrezime").value("Pera Peric"));
    }

    @Test
    void komentariResenjaSuPoredjaniPoFajluILiniji() throws Exception {
        dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 30, "treci"), nastavnikRS()).andExpect(status().isOk());
        dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 5, "drugi"), nastavnikRS()).andExpect(status().isOk());
        dodajKomentar(resenjePrvog, komentarJson("src/App.java", 99, "prvi"), nastavnikRS()).andExpect(status().isOk());

        mockMvc.perform(get("/api/resenja/" + resenjePrvog).with(nastavnikRS()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.komentari.length()").value(3))
                .andExpect(jsonPath("$.komentari[0].tekst").value("prvi"))
                .andExpect(jsonPath("$.komentari[1].tekst").value("drugi"))
                .andExpect(jsonPath("$.komentari[2].tekst").value("treci"));
    }

    @Test
    void neispravanKomentarVraca400() throws Exception {
        dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 12, ""), nastavnikRS()).andExpect(status().isBadRequest());
        dodajKomentar(resenjePrvog, komentarJson("", 12, "tekst"), nastavnikRS()).andExpect(status().isBadRequest());
        dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 0, "tekst"), nastavnikRS()).andExpect(status().isBadRequest());
    }

    @Test
    void komentarNaNepredatoResenjeVraca409() throws Exception {
        dodajKomentar(resenjeCetvrtog, komentarJson("src/Main.java", 1, "tekst"), nastavnikRS())
                .andExpect(status().isConflict());
    }

    @Test
    void komentarNeMozeDaDodaNastavnikKojiNePredajeNiStudent() throws Exception {
        String json = komentarJson("src/Main.java", 1, "tekst");

        dodajKomentar(resenjePrvog, json, kaoNastavnik(drugiNastavnik.getId())).andExpect(status().isForbidden());
        dodajKomentar(resenjePrvog, json, kaoStudent(prviStudent.getId())).andExpect(status().isForbidden());
    }

    @Test
    void nastavnikMenjaKomentar() throws Exception {
        Long komentarId = idKomentara(dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 12, "stari tekst"), nastavnikRS()));

        mockMvc.perform(put("/api/resenja/" + resenjePrvog + "/komentari/" + komentarId).with(nastavnikRS())
                        .contentType(MediaType.APPLICATION_JSON).content(komentarJson("src/Main.java", 14, "novi tekst")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(komentarId.intValue()))
                .andExpect(jsonPath("$.linija").value(14))
                .andExpect(jsonPath("$.tekst").value("novi tekst"));
    }

    @Test
    void obrisanKomentarNestajeIzResenja() throws Exception {
        Long komentarId = idKomentara(dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 12, "tekst"), nastavnikRS()));

        mockMvc.perform(delete("/api/resenja/" + resenjePrvog + "/komentari/" + komentarId).with(nastavnikRS()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/resenja/" + resenjePrvog).with(nastavnikRS()))
                .andExpect(jsonPath("$.komentari.length()").value(0));
    }

    @Test
    void komentarSeNeMozeMenjatiPrekoTudjegResenja() throws Exception {
        Long komentarId = idKomentara(dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 12, "tekst"), nastavnikRS()));

        mockMvc.perform(delete("/api/resenja/" + resenjeDrugog + "/komentari/" + komentarId).with(nastavnikRS()))
                .andExpect(status().isNotFound());
    }

    // ---------- student vidi samo svoje ----------

    @Test
    void studentVidiSvojePoeneIKomentare() throws Exception {
        upisiPoene(resenjePrvog, 85, nastavnikRS()).andExpect(status().isOk());
        dodajKomentar(resenjePrvog, komentarJson("src/Main.java", 12, "Ovde fali provera za null"), nastavnikRS())
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/resenja/" + resenjePrvog).with(kaoStudent(prviStudent.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indeks").value("RN 1/2023"))
                .andExpect(jsonPath("$.poeni").value(85))
                .andExpect(jsonPath("$.komentari.length()").value(1))
                .andExpect(jsonPath("$.komentari[0].putanjaFajla").value("src/Main.java"))
                .andExpect(jsonPath("$.komentari[0].linija").value(12))
                .andExpect(jsonPath("$.komentari[0].tekst").value("Ovde fali provera za null"));
    }

    @Test
    void studentNeVidiTudjeResenje() throws Exception {
        upisiPoene(resenjeDrugog, 40, nastavnikRS()).andExpect(status().isOk());

        mockMvc.perform(get("/api/resenja/" + resenjeDrugog).with(kaoStudent(prviStudent.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", containsString("samo svoje")));
    }

    @Test
    void studentDoIdjaSvogResenjaDolaziPrekoPolaganja() throws Exception {
        mockMvc.perform(get("/api/polaganja/" + grupaA.getId()).with(kaoStudent(prviStudent.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resenjeId").value(resenjePrvog.intValue()));
    }

    @Test
    void studentNeMozeDaPretrazujeNiDaPreuzimaResenja() throws Exception {
        RequestPostProcessor student = kaoStudent(prviStudent.getId());

        mockMvc.perform(get("/api/resenja").with(student)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/resenja/zip?grupaId=" + grupaA.getId()).with(student)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/resenja/" + resenjePrvog + "/fajl").with(student)).andExpect(status().isForbidden());
    }

    @Test
    void nastavnikKojiNePredajePredmetNeVidiResenje() throws Exception {
        mockMvc.perform(get("/api/resenja/" + resenjePrvog).with(kaoNastavnik(drugiNastavnik.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void nepostojeceResenjeVraca404AZahtevBezTokena401() throws Exception {
        mockMvc.perform(get("/api/resenja/999999").with(nastavnikRS())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/resenja/" + resenjePrvog)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/resenja")).andExpect(status().isUnauthorized());
    }
}
