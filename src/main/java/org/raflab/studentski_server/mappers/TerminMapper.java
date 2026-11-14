package org.raflab.studentski_server.mappers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.TerminRequest;
import org.raflab.studentski_server.controllers.response.TerminResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.exceptions.BadRequestException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.Ispit;
import org.raflab.studentski_server.model.Termin;
import org.raflab.studentski_server.repositories.IspitRepository;
import org.springframework.stereotype.Component;

/*
 * Primer mappera za entitet sa vezom: zahtev nosi ispitId, a mapper nalazi ispit u bazi.
 */
@Component
@AllArgsConstructor
public class TerminMapper implements CrudMapper<Termin, TerminRequest, TerminResponse> {

    final IspitRepository ispitRepository;

    @Override
    public Termin createEntityFromRequest(TerminRequest request) {
        Termin termin = new Termin();
        updateExistingEntityFromRequest(termin, request);
        return termin;
    }

    @Override
    public void updateExistingEntityFromRequest(Termin termin, TerminRequest request) {
        if (!request.kraj().isAfter(request.pocetak())) {
            throw new BadRequestException("Kraj termina mora biti posle pocetka");
        }
        Ispit ispit = ispitRepository.findByIdAndObrisanFalse(request.ispitId())
                .orElseThrow(() -> new ResourceNotFoundException("Ispit " + request.ispitId() + " ne postoji"));
        termin.setIspit(ispit);
        termin.setRedniBroj(request.redniBroj());
        termin.setPocetak(request.pocetak());
        termin.setKraj(request.kraj());
    }

    @Override
    public TerminResponse toResponse(Termin termin) {
        Ispit ispit = termin.getIspit();
        return new TerminResponse(termin.getId(), ispit.getId(),
                ispit.getPredmet().getSifra(),
                ispit.getIspitniRok().getNaziv(),
                ispit.getIspitniRok().getSkolskaGodina(),
                termin.getRedniBroj(), termin.getPocetak(), termin.getKraj());
    }
}
