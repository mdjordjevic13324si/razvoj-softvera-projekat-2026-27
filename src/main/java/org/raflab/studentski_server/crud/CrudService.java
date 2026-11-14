package org.raflab.studentski_server.crud;

import org.raflab.studentski_server.controllers.response.PageResponse;
import org.raflab.studentski_server.exceptions.BadRequestException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.Obrisiv;
import org.raflab.studentski_server.repositories.ObrisivRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/*
 * Kalup za CRUD servis - pise se jednom, a svaki entitet ga nasledi, npr.
 *   public class UcionicaService extends CrudService<Ucionica, UcionicaRequest, UcionicaResponse>
 *
 * Kalup zna samo dve stvari o entitetu: da ima soft brisanje (Obrisiv) i kako se
 * pretvara iz zahteva i u odgovor (CrudMapper). Sve ostalo je isto za svaki entitet.
 *
 * Metode vracaju RESPONSE (DTO), pa entitet nikad ne izlazi iz servisa.
 */
public abstract class CrudService<ENTITY extends Obrisiv, REQUEST, RESPONSE> {

    public static final int NAJVECA_STRANICA = 100;

    protected final ObrisivRepository<ENTITY> repository;
    protected final CrudMapper<ENTITY, REQUEST, RESPONSE> mapper;
    private final String nazivEntiteta;   // za poruke o gresci, npr. "Ucionica 5 ne postoji"

    protected CrudService(ObrisivRepository<ENTITY> repository,
                          CrudMapper<ENTITY, REQUEST, RESPONSE> mapper,
                          String nazivEntiteta) {
        this.repository = repository;
        this.mapper = mapper;
        this.nazivEntiteta = nazivEntiteta;
    }

    // svi neobrisani, po stranicama, poredjani po id-ju
    @Transactional(readOnly = true)
    public PageResponse<RESPONSE> nadjiSve(int brojStranice, int velicinaStranice) {
        if (brojStranice < 0 || velicinaStranice < 1 || velicinaStranice > NAJVECA_STRANICA) {
            throw new BadRequestException("page mora biti 0 ili vise, a size izmedju 1 i " + NAJVECA_STRANICA);
        }
        Page<ENTITY> stranica = repository.findAllByObrisanFalse(
                PageRequest.of(brojStranice, velicinaStranice, Sort.by("id")));
        return PageResponse.of(stranica, mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RESPONSE nadji(Long id) {
        return mapper.toResponse(nadjiEntitet(id));
    }

    @Transactional
    public RESPONSE napravi(REQUEST request) {
        ENTITY noviEntitet = mapper.createEntityFromRequest(request);
        // saveAndFlush salje INSERT odmah, pa se duplikat (409) prijavi ovde, a ne tek na kraju transakcije
        return mapper.toResponse(repository.saveAndFlush(noviEntitet));
    }

    @Transactional
    public RESPONSE izmeni(Long id, REQUEST request) {
        ENTITY postojeciEntitet = nadjiEntitet(id);
        mapper.updateExistingEntityFromRequest(postojeciEntitet, request);
        return mapper.toResponse(repository.saveAndFlush(postojeciEntitet));
    }

    // soft brisanje: red ostaje u bazi, samo se oznaci kao obrisan
    @Transactional
    public void obrisi(Long id) {
        ENTITY entitet = nadjiEntitet(id);
        entitet.setObrisan(true);
        repository.save(entitet);
    }

    // obrisan entitet se ponasa kao da ne postoji (404)
    protected ENTITY nadjiEntitet(Long id) {
        return repository.findByIdAndObrisanFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(nazivEntiteta + " " + id + " ne postoji"));
    }
}
