package org.raflab.studentski_server.exceptions;

/*
 * Korisnik je prijavljen, ali nema pravo na ovu akciju (npr. nastavnik ne predaje taj predmet) - 403
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
