package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.PredajePredmet;

public interface PredajePredmetRepository extends ObrisivRepository<PredajePredmet> {

    // da li nastavnik predaje predmet (i time sme da upravlja njegovim zadacima i spiskovima)
    boolean existsByNastavnikIdAndPredmetIdAndObrisanFalse(Long nastavnikId, Long predmetId);
}
