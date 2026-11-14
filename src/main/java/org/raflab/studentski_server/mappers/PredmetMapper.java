package org.raflab.studentski_server.mappers;

import org.raflab.studentski_server.controllers.request.PredmetRequest;
import org.raflab.studentski_server.controllers.response.PredmetResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.model.Predmet;
import org.springframework.stereotype.Component;

@Component
public class PredmetMapper implements CrudMapper<Predmet, PredmetRequest, PredmetResponse> {

    @Override
    public Predmet createEntityFromRequest(PredmetRequest request) {
        Predmet predmet = new Predmet();
        updateExistingEntityFromRequest(predmet, request);
        return predmet;
    }

    @Override
    public void updateExistingEntityFromRequest(Predmet predmet, PredmetRequest request) {
        predmet.setSifra(request.sifra().trim());
        predmet.setNaziv(request.naziv().trim());
    }

    @Override
    public PredmetResponse toResponse(Predmet predmet) {
        return new PredmetResponse(predmet.getId(), predmet.getSifra(), predmet.getNaziv());
    }
}
