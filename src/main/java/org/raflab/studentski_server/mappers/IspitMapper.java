package org.raflab.studentski_server.mappers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.IspitRequest;
import org.raflab.studentski_server.controllers.response.IspitResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.Ispit;
import org.raflab.studentski_server.model.IspitniRok;
import org.raflab.studentski_server.model.Predmet;
import org.raflab.studentski_server.repositories.IspitniRokRepository;
import org.raflab.studentski_server.repositories.PredmetRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IspitMapper implements CrudMapper<Ispit, IspitRequest, IspitResponse> {

    final IspitniRokRepository ispitniRokRepository;
    final PredmetRepository predmetRepository;

    @Override
    public Ispit createEntityFromRequest(IspitRequest request) {
        Ispit ispit = new Ispit();
        updateExistingEntityFromRequest(ispit, request);
        return ispit;
    }

    @Override
    public void updateExistingEntityFromRequest(Ispit ispit, IspitRequest request) {
        IspitniRok ispitniRok = ispitniRokRepository.findByIdAndObrisanFalse(request.ispitniRokId())
                .orElseThrow(() -> new ResourceNotFoundException("Ispitni rok " + request.ispitniRokId() + " ne postoji"));
        Predmet predmet = predmetRepository.findByIdAndObrisanFalse(request.predmetId())
                .orElseThrow(() -> new ResourceNotFoundException("Predmet " + request.predmetId() + " ne postoji"));
        ispit.setIspitniRok(ispitniRok);
        ispit.setPredmet(predmet);
        ispit.setDatum(request.datum());
    }

    @Override
    public IspitResponse toResponse(Ispit ispit) {
        IspitniRok ispitniRok = ispit.getIspitniRok();
        Predmet predmet = ispit.getPredmet();
        return new IspitResponse(ispit.getId(),
                ispitniRok.getId(), ispitniRok.getNaziv(), ispitniRok.getSkolskaGodina(),
                predmet.getId(), predmet.getSifra(), predmet.getNaziv(),
                ispit.getDatum());
    }
}
