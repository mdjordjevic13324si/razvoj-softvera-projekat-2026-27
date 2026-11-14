package org.raflab.studentski_server.mappers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.PredajePredmetRequest;
import org.raflab.studentski_server.controllers.response.PredajePredmetResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.Nastavnik;
import org.raflab.studentski_server.model.PredajePredmet;
import org.raflab.studentski_server.model.Predmet;
import org.raflab.studentski_server.repositories.NastavnikRepository;
import org.raflab.studentski_server.repositories.PredmetRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PredajePredmetMapper implements CrudMapper<PredajePredmet, PredajePredmetRequest, PredajePredmetResponse> {

    final NastavnikRepository nastavnikRepository;
    final PredmetRepository predmetRepository;

    @Override
    public PredajePredmet createEntityFromRequest(PredajePredmetRequest request) {
        PredajePredmet predajePredmet = new PredajePredmet();
        updateExistingEntityFromRequest(predajePredmet, request);
        return predajePredmet;
    }

    @Override
    public void updateExistingEntityFromRequest(PredajePredmet predajePredmet, PredajePredmetRequest request) {
        Nastavnik nastavnik = nastavnikRepository.findByIdAndObrisanFalse(request.nastavnikId())
                .orElseThrow(() -> new ResourceNotFoundException("Nastavnik " + request.nastavnikId() + " ne postoji"));
        Predmet predmet = predmetRepository.findByIdAndObrisanFalse(request.predmetId())
                .orElseThrow(() -> new ResourceNotFoundException("Predmet " + request.predmetId() + " ne postoji"));
        predajePredmet.setNastavnik(nastavnik);
        predajePredmet.setPredmet(predmet);
    }

    @Override
    public PredajePredmetResponse toResponse(PredajePredmet predajePredmet) {
        Nastavnik nastavnik = predajePredmet.getNastavnik();
        Predmet predmet = predajePredmet.getPredmet();
        return new PredajePredmetResponse(predajePredmet.getId(),
                nastavnik.getId(), nastavnik.getIme() + " " + nastavnik.getPrezime(),
                predmet.getId(), predmet.getSifra(), predmet.getNaziv());
    }
}
