package org.raflab.studentski_server.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.SpisakRequest;
import org.raflab.studentski_server.controllers.response.SpisakStavkaResponse;
import org.raflab.studentski_server.services.SpisakService;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Spisak za polaganje jedne grupe - samo nastavnik (pravilo je u SecurityConfig)
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/grupe/{grupaId}/spisak")
public class SpisakController {

    final SpisakService spisakService;

    @PostMapping
    public List<SpisakStavkaResponse> unesiSpisak(@PathVariable("grupaId") Long grupaId,
                                                  @RequestBody @Valid SpisakRequest request) {
        return spisakService.unesi(grupaId, request.getStavke()).stream()
                .map(Converters::toSpisakStavkaResponse)
                .toList();
    }

    @GetMapping
    public List<SpisakStavkaResponse> spisakGrupe(@PathVariable("grupaId") Long grupaId) {
        return spisakService.spisakGrupe(grupaId).stream()
                .map(Converters::toSpisakStavkaResponse)
                .toList();
    }

    @DeleteMapping(path = "/{stavkaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisiSaSpiska(@PathVariable("grupaId") Long grupaId, @PathVariable("stavkaId") Long stavkaId) {
        spisakService.obrisi(grupaId, stavkaId);
    }
}
