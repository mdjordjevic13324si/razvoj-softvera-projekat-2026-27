package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Monitoring jedne grupe: zbirni brojevi i red za svakog studenta sa spiska.
 * prosecnoVremeIzradeSekundi je prosek za studente koji su predali; null ako niko nije predao.
 */
public record MonitoringGrupeResponse(
        Long grupaId,
        String grupaOznaka,
        Long terminId,
        int terminRedniBroj,
        String predmetSifra,
        String predmetNaziv,
        String ispitniRokNaziv,
        LocalDateTime pocetak,
        LocalDateTime kraj,
        StanjeTermina stanjeTermina,
        boolean zadatakPostavljen,
        int brojNaSpisku,
        int brojPreuzelih,
        int brojPredalih,
        Long prosecnoVremeIzradeSekundi,
        List<MonitoringStavkaResponse> studenti) {
}
