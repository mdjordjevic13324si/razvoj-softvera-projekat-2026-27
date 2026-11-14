package org.raflab.studentski_server.controllers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.response.ZadatakResponse;
import org.raflab.studentski_server.services.ZadatakService;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
 * Zadaci - samo nastavnik (pravilo je u SecurityConfig), i to onaj koji predaje predmet (provera u servisu)
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/zadaci")
public class ZadatakController {

    final ZadatakService zadatakService;

    // multipart/form-data: polja grupaId, naslov, opis (opciono) i fajl (zip projekta)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ZadatakResponse postaviZadatak(@RequestParam("grupaId") Long grupaId,
                                          @RequestParam("naslov") String naslov,
                                          @RequestParam(name = "opis", required = false) String opis,
                                          @RequestParam("fajl") MultipartFile fajl) {
        return Converters.toZadatakResponse(zadatakService.postavi(grupaId, naslov, opis, fajl));
    }

    @GetMapping(path = "/{zadatakId}")
    public ZadatakResponse nadjiZadatak(@PathVariable("zadatakId") Long zadatakId) {
        return Converters.toZadatakResponse(zadatakService.nadji(zadatakId));
    }

    // zip zadatka, da nastavnik moze da proveri sta je postavio
    @GetMapping(path = "/{zadatakId}/fajl")
    public ResponseEntity<Resource> preuzmiFajlZadatka(@PathVariable("zadatakId") Long zadatakId) {
        return Converters.toZipResponse(zadatakService.fajl(zadatakId));
    }

    @DeleteMapping(path = "/{zadatakId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisiZadatak(@PathVariable("zadatakId") Long zadatakId) {
        zadatakService.obrisi(zadatakId);
    }
}
