package org.raflab.studentski_server.controllers.response;

import org.raflab.studentski_server.model.VrstaSmera;

public record SmerResponse(Long id, String oznaka, String naziv, VrstaSmera vrsta) {
}
