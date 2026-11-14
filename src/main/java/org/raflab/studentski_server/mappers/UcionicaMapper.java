package org.raflab.studentski_server.mappers;

import org.raflab.studentski_server.controllers.request.UcionicaRequest;
import org.raflab.studentski_server.controllers.response.UcionicaResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.model.Ucionica;
import org.springframework.stereotype.Component;

@Component
public class UcionicaMapper implements CrudMapper<Ucionica, UcionicaRequest, UcionicaResponse> {

    @Override
    public Ucionica createEntityFromRequest(UcionicaRequest request) {
        Ucionica ucionica = new Ucionica();
        updateExistingEntityFromRequest(ucionica, request);
        return ucionica;
    }

    @Override
    public void updateExistingEntityFromRequest(Ucionica ucionica, UcionicaRequest request) {
        ucionica.setOznaka(request.oznaka().trim());
        ucionica.setAdresa(request.adresa().trim());
        ucionica.setSprat(request.sprat());
        ucionica.setKapacitet(request.kapacitet());
        ucionica.setBrojRacunara(request.brojRacunara());
    }

    @Override
    public UcionicaResponse toResponse(Ucionica ucionica) {
        return new UcionicaResponse(ucionica.getId(), ucionica.getOznaka(), ucionica.getAdresa(),
                ucionica.getSprat(), ucionica.getKapacitet(), ucionica.getBrojRacunara());
    }
}
