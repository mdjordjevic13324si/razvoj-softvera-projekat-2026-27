package org.raflab.studentski_server.mappers;

import org.raflab.studentski_server.controllers.request.NastavnikRequest;
import org.raflab.studentski_server.controllers.response.NastavnikResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.model.Nastavnik;
import org.springframework.stereotype.Component;

@Component
public class NastavnikMapper implements CrudMapper<Nastavnik, NastavnikRequest, NastavnikResponse> {

    @Override
    public Nastavnik createEntityFromRequest(NastavnikRequest request) {
        Nastavnik nastavnik = new Nastavnik();
        updateExistingEntityFromRequest(nastavnik, request);
        return nastavnik;
    }

    @Override
    public void updateExistingEntityFromRequest(Nastavnik nastavnik, NastavnikRequest request) {
        nastavnik.setIme(request.ime().trim());
        nastavnik.setPrezime(request.prezime().trim());
        nastavnik.setEmail(request.email().trim());
    }

    @Override
    public NastavnikResponse toResponse(Nastavnik nastavnik) {
        return new NastavnikResponse(nastavnik.getId(), nastavnik.getIme(), nastavnik.getPrezime(), nastavnik.getEmail());
    }
}
