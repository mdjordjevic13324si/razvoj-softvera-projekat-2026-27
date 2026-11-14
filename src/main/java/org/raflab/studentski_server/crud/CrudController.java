package org.raflab.studentski_server.crud;

import jakarta.validation.Valid;
import org.raflab.studentski_server.controllers.response.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/*
 * Kalup za CRUD kontroler - pise se jednom. Konkretan kontroler samo kaze URL i servis, npr.
 *
 *   @RestController
 *   @RequestMapping("/api/ucionice")
 *   public class UcionicaController extends CrudController<UcionicaRequest, UcionicaResponse>
 *
 * i time dobija pet endpointa:
 *   GET    /api/ucionice?page=0&size=10   lista po stranicama
 *   GET    /api/ucionice/{id}             jedan
 *   POST   /api/ucionice                  pravljenje
 *   PUT    /api/ucionice/{id}             izmena
 *   DELETE /api/ucionice/{id}             soft brisanje
 *
 * Ko sme sta odredjuje SecurityConfig: GET svi prijavljeni, ostalo samo nastavnik.
 */
public abstract class CrudController<REQUEST, RESPONSE> {

    private final CrudService<?, REQUEST, RESPONSE> service;

    protected CrudController(CrudService<?, REQUEST, RESPONSE> service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<RESPONSE> nadjiSve(@RequestParam(name = "page", defaultValue = "0") int page,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        return service.nadjiSve(page, size);
    }

    @GetMapping(path = "/{id}")
    public RESPONSE nadji(@PathVariable("id") Long id) {
        return service.nadji(id);
    }

    @PostMapping
    public RESPONSE napravi(@RequestBody @Valid REQUEST request) {
        return service.napravi(request);
    }

    @PutMapping(path = "/{id}")
    public RESPONSE izmeni(@PathVariable("id") Long id, @RequestBody @Valid REQUEST request) {
        return service.izmeni(id, request);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable("id") Long id) {
        service.obrisi(id);
    }
}
