package org.raflab.studentski_server.mappers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.GrupaRequest;
import org.raflab.studentski_server.controllers.response.GrupaResponse;
import org.raflab.studentski_server.crud.CrudMapper;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.Grupa;
import org.raflab.studentski_server.model.Termin;
import org.raflab.studentski_server.repositories.TerminRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GrupaMapper implements CrudMapper<Grupa, GrupaRequest, GrupaResponse> {

    final TerminRepository terminRepository;

    @Override
    public Grupa createEntityFromRequest(GrupaRequest request) {
        Grupa grupa = new Grupa();
        updateExistingEntityFromRequest(grupa, request);
        return grupa;
    }

    @Override
    public void updateExistingEntityFromRequest(Grupa grupa, GrupaRequest request) {
        Termin termin = terminRepository.findByIdAndObrisanFalse(request.terminId())
                .orElseThrow(() -> new ResourceNotFoundException("Termin " + request.terminId() + " ne postoji"));
        grupa.setTermin(termin);
        grupa.setOznaka(request.oznaka().trim());
    }

    @Override
    public GrupaResponse toResponse(Grupa grupa) {
        Termin termin = grupa.getTermin();
        return new GrupaResponse(grupa.getId(), grupa.getOznaka(),
                termin.getId(), termin.getRedniBroj(),
                termin.getIspit().getId(), termin.getIspit().getPredmet().getSifra());
    }
}
