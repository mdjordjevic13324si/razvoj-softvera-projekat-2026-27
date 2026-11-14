package org.raflab.studentski_server.controllers.response;

import lombok.Data;

/*
 * Zadatak sa svim cemu je dodeljen: grupa -> termin -> ispit (predmet + ispitni rok).
 * Putanja zipa na disku se ne salje klijentu.
 */
@Data
public class ZadatakResponse {

    private Long id;
    private String naslov;
    private String opis;
    private String originalnoIme;

    private Long grupaId;
    private String grupaOznaka;
    private Long terminId;
    private int terminRedniBroj;
    private Long ispitId;
    private String predmetSifra;
    private String predmetNaziv;
    private String ispitniRokNaziv;
    private String skolskaGodina;

    private Long nastavnikId;
    private String nastavnikImeIPrezime;
}
