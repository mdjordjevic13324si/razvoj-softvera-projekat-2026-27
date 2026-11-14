package org.raflab.studentski_server.mappers;

import org.raflab.studentski_server.controllers.request.IspitniRokRequest;
import org.raflab.studentski_server.controllers.response.IspitniRokResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.model.IspitniRok;
import org.springframework.stereotype.Component;

@Component
public class IspitniRokMapper implements CrudMapper<IspitniRok, IspitniRokRequest, IspitniRokResponse> {

    @Override
    public IspitniRok createEntityFromRequest(IspitniRokRequest request) {
        IspitniRok ispitniRok = new IspitniRok();
        updateExistingEntityFromRequest(ispitniRok, request);
        return ispitniRok;
    }

    @Override
    public void updateExistingEntityFromRequest(IspitniRok ispitniRok, IspitniRokRequest request) {
        ispitniRok.setSkolskaGodina(request.skolskaGodina().trim());
        ispitniRok.setNaziv(request.naziv().trim());
        ispitniRok.setTip(request.tip());
    }

    @Override
    public IspitniRokResponse toResponse(IspitniRok ispitniRok) {
        return new IspitniRokResponse(ispitniRok.getId(), ispitniRok.getSkolskaGodina(), ispitniRok.getNaziv(), ispitniRok.getTip());
    }
}
