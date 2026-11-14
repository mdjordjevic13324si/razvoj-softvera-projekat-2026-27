package org.raflab.studentski_server.utils;

import org.raflab.studentski_server.controllers.response.SpisakStavkaResponse;
import org.raflab.studentski_server.controllers.response.ZadatakResponse;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.services.PreuzetFajl;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/*
 * Pretvaranje entiteta u response DTO-e - entitet nikad ne izlazi iz servera
 */
public class Converters {

    public static ZadatakResponse toZadatakResponse(Zadatak zadatak) {
        Grupa grupa = zadatak.getGrupa();
        Termin termin = grupa.getTermin();
        Ispit ispit = termin.getIspit();

        ZadatakResponse response = new ZadatakResponse();
        response.setId(zadatak.getId());
        response.setNaslov(zadatak.getNaslov());
        response.setOpis(zadatak.getOpis());
        response.setOriginalnoIme(zadatak.getOriginalnoIme());
        response.setGrupaId(grupa.getId());
        response.setGrupaOznaka(grupa.getOznaka());
        response.setTerminId(termin.getId());
        response.setTerminRedniBroj(termin.getRedniBroj());
        response.setIspitId(ispit.getId());
        response.setPredmetSifra(ispit.getPredmet().getSifra());
        response.setPredmetNaziv(ispit.getPredmet().getNaziv());
        response.setIspitniRokNaziv(ispit.getIspitniRok().getNaziv());
        response.setSkolskaGodina(ispit.getIspitniRok().getSkolskaGodina());
        response.setNastavnikId(zadatak.getNastavnik().getId());
        response.setNastavnikImeIPrezime(zadatak.getNastavnik().getIme() + " " + zadatak.getNastavnik().getPrezime());
        return response;
    }

    public static SpisakStavkaResponse toSpisakStavkaResponse(SpisakStavka stavka) {
        Student student = stavka.getStudent();

        SpisakStavkaResponse response = new SpisakStavkaResponse();
        response.setId(stavka.getId());
        response.setStudentId(student.getId());
        response.setIme(student.getIme());
        response.setPrezime(student.getPrezime());
        response.setIndeks(indeks(student));
        response.setGrupaId(stavka.getGrupa().getId());
        response.setGrupaOznaka(stavka.getGrupa().getOznaka());
        response.setUcionicaOznaka(stavka.getUcionica().getOznaka());
        return response;
    }

    // HTTP odgovor koji klijent cuva kao zip fajl pod datim imenom
    public static ResponseEntity<Resource> toZipResponse(PreuzetFajl fajl) {
        ContentDisposition kaoPrilog = ContentDisposition.attachment()
                .filename(fajl.ime(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, kaoPrilog.toString())
                .body(fajl.sadrzaj());
    }

    // npr. RN 12/2023
    public static String indeks(Student student) {
        return student.getSmer().getOznaka() + " " + student.getBrojIndeksa() + "/" + student.getGodinaUpisa();
    }
}
