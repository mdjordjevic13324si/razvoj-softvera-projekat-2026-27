package org.raflab.studentski_server.mappers;

import org.raflab.studentski_server.controllers.request.SmerRequest;
import org.raflab.studentski_server.controllers.response.SmerResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.model.Smer;
import org.springframework.stereotype.Component;

@Component
public class SmerMapper implements CrudMapper<Smer, SmerRequest, SmerResponse> {

    @Override
    public Smer createEntityFromRequest(SmerRequest request) {
        Smer smer = new Smer();
        updateExistingEntityFromRequest(smer, request);
        return smer;
    }

    @Override
    public void updateExistingEntityFromRequest(Smer smer, SmerRequest request) {
        smer.setOznaka(request.oznaka().trim());
        smer.setNaziv(request.naziv().trim());
        smer.setVrsta(request.vrsta());
    }

    @Override
    public SmerResponse toResponse(Smer smer) {
        return new SmerResponse(smer.getId(), smer.getOznaka(), smer.getNaziv(), smer.getVrsta());
    }
}
