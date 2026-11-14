package org.raflab.studentski_server.controllers.response;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Monitoring celog termina: zbir svih grupa i svaka grupa posebno.
 * Prosek se racuna nad svim studentima termina koji su predali (nije prosek proseka grupa).
 */
public record MonitoringTerminaResponse(
        Long terminId,
        int terminRedniBroj,
        String predmetSifra,
        String predmetNaziv,
        String ispitniRokNaziv,
        LocalDateTime pocetak,
        LocalDateTime kraj,
        StanjeTermina stanjeTermina,
        int brojNaSpisku,
        int brojPreuzelih,
        int brojPredalih,
        Long prosecnoVremeIzradeSekundi,
        List<MonitoringGrupeResponse> grupe) {
}
