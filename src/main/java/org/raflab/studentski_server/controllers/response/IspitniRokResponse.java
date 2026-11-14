package org.raflab.studentski_server.controllers.response;

import org.raflab.studentski_server.model.TipRoka;

public record IspitniRokResponse(Long id, String skolskaGodina, String naziv, TipRoka tip) {
}
