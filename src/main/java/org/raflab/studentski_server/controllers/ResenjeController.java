package org.raflab.studentski_server.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.KomentarRequest;
import org.raflab.studentski_server.controllers.request.PoeniRequest;
import org.raflab.studentski_server.controllers.response.KomentarResponse;
import org.raflab.studentski_server.controllers.response.ResenjeResponse;
import org.raflab.studentski_server.services.OcenjivanjeService;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Pregled i ocenjivanje resenja.
 * Sve je samo za nastavnika, osim GET /api/resenja/{id}: njega sme i student, ali samo za svoje resenje
 * (pravila su u SecurityConfig, a provere vlasnistva u PristupService).
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/resenja")
public class ResenjeController {

    final OcenjivanjeService ocenjivanjeService;

    // predata resenja po predmetu, roku, terminu i grupi; svaki parametar je opcion
    @GetMapping
    public List<ResenjeResponse> pretraziResenja(@RequestParam(name = "predmetId", required = false) Long predmetId,
                                                 @RequestParam(name = "ispitniRokId", required = false) Long ispitniRokId,
                                                 @RequestParam(name = "terminId", required = false) Long terminId,
                                                 @RequestParam(name = "grupaId", required = false) Long grupaId) {
        return ocenjivanjeService.pretrazi(predmetId, ispitniRokId, terminId, grupaId);
    }

    // ista resenja kao u pretrazi, ali kao jedan zip za preuzimanje
    @GetMapping(path = "/zip")
    public ResponseEntity<Resource> preuzmiResenja(@RequestParam(name = "predmetId", required = false) Long predmetId,
                                                   @RequestParam(name = "ispitniRokId", required = false) Long ispitniRokId,
                                                   @RequestParam(name = "terminId", required = false) Long terminId,
                                                   @RequestParam(name = "grupaId", required = false) Long grupaId) {
        return Converters.toZipResponse(ocenjivanjeService.zip(predmetId, ispitniRokId, terminId, grupaId));
    }

    // resenje sa poenima i komentarima - nastavnik, ili student kome pripada
    @GetMapping(path = "/{resenjeId}")
    public ResenjeResponse nadjiResenje(@PathVariable("resenjeId") Long resenjeId) {
        return ocenjivanjeService.nadji(resenjeId);
    }

    @GetMapping(path = "/{resenjeId}/fajl")
    public ResponseEntity<Resource> preuzmiResenje(@PathVariable("resenjeId") Long resenjeId) {
        return Converters.toZipResponse(ocenjivanjeService.fajl(resenjeId));
    }

    @PutMapping(path = "/{resenjeId}/poeni")
    public ResenjeResponse upisiPoene(@PathVariable("resenjeId") Long resenjeId, @RequestBody @Valid PoeniRequest request) {
        return ocenjivanjeService.upisiPoene(resenjeId, request.poeni());
    }

    @PostMapping(path = "/{resenjeId}/komentari")
    public KomentarResponse dodajKomentar(@PathVariable("resenjeId") Long resenjeId, @RequestBody @Valid KomentarRequest request) {
        return ocenjivanjeService.dodajKomentar(resenjeId, request);
    }

    @PutMapping(path = "/{resenjeId}/komentari/{komentarId}")
    public KomentarResponse izmeniKomentar(@PathVariable("resenjeId") Long resenjeId,
                                           @PathVariable("komentarId") Long komentarId,
                                           @RequestBody @Valid KomentarRequest request) {
        return ocenjivanjeService.izmeniKomentar(resenjeId, komentarId, request);
    }

    @DeleteMapping(path = "/{resenjeId}/komentari/{komentarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisiKomentar(@PathVariable("resenjeId") Long resenjeId, @PathVariable("komentarId") Long komentarId) {
        ocenjivanjeService.obrisiKomentar(resenjeId, komentarId);
    }
}
