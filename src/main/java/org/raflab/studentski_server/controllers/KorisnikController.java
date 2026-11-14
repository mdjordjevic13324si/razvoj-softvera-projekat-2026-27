package org.raflab.studentski_server.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.KorisnikRequest;
import org.raflab.studentski_server.controllers.response.KorisnikResponse;
import org.raflab.studentski_server.controllers.response.PageResponse;
import org.raflab.studentski_server.services.KorisnikService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*
 * Nalozi za login - rucno napisan CRUD (bez kalupa).
 * Ceo kontroler je samo za nastavnika, ukljucujuci GET: SecurityConfig pusta GET svima
 * koji su ulogovani, a student ne sme da vidi tudje naloge.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/korisnici")
@PreAuthorize("hasRole('NASTAVNIK')")
public class KorisnikController {

    final KorisnikService korisnikService;

    @GetMapping
    public PageResponse<KorisnikResponse> nadjiSve(@RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        return korisnikService.nadjiSve(page, size);
    }

    @GetMapping(path = "/{id}")
    public KorisnikResponse nadji(@PathVariable("id") Long id) {
        return korisnikService.nadji(id);
    }

    @PostMapping
    public KorisnikResponse napravi(@RequestBody @Valid KorisnikRequest request) {
        return korisnikService.napravi(request);
    }

    @PutMapping(path = "/{id}")
    public KorisnikResponse izmeni(@PathVariable("id") Long id, @RequestBody @Valid KorisnikRequest request) {
        return korisnikService.izmeni(id, request);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable("id") Long id) {
        korisnikService.obrisi(id);
    }
}
