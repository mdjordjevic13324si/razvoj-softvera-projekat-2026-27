package org.raflab.studentski_server.controllers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.response.PolaganjeResponse;
import org.raflab.studentski_server.controllers.response.PredajaResponse;
import org.raflab.studentski_server.services.PolaganjeService;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 * Polaganje iz ugla studenta (pregled, preuzimanje zadatka, predaja resenja) - samo uloga STUDENT (pravilo je u SecurityConfig).
 * Student se uzima iz tokena, pa svako vidi i preuzima samo svoje.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/polaganja")
public class PolaganjeController {

    final PolaganjeService polaganjeService;

    // sva moja polaganja
    @GetMapping
    public List<PolaganjeResponse> mojaPolaganja() {
        return polaganjeService.mojaPolaganja();
    }

    // provera da li sam na spisku grupe, sa podacima o terminu
    @GetMapping(path = "/{grupaId}")
    public PolaganjeResponse mojePolaganje(@PathVariable("grupaId") Long grupaId) {
        return polaganjeService.mojePolaganje(grupaId);
    }

    // POST, a ne GET, jer prvo preuzimanje upisuje vreme u bazu
    @PostMapping(path = "/{grupaId}/preuzimanje")
    public ResponseEntity<Resource> preuzmiZadatak(@PathVariable("grupaId") Long grupaId) {
        return Converters.toZipResponse(polaganjeService.preuzmiZadatak(grupaId));
    }

    // multipart/form-data sa poljem fajl (zip resenja); moze vise puta, do kraja termina
    @PostMapping(path = "/{grupaId}/predaja", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PredajaResponse predajResenje(@PathVariable("grupaId") Long grupaId,
                                         @RequestParam("fajl") MultipartFile fajl) {
        return polaganjeService.predajResenje(grupaId, fajl);
    }

    // moj poslednji predati zip
    @GetMapping(path = "/{grupaId}/resenje")
    public ResponseEntity<Resource> mojeResenje(@PathVariable("grupaId") Long grupaId) {
        return Converters.toZipResponse(polaganjeService.mojeResenje(grupaId));
    }
}
