package org.raflab.studentski_server.model;

/*
 * Entitet koji se ne brise iz baze, vec se samo oznaci kao obrisan (soft delete).
 * Red ostaje u tabeli, pa veze ka njemu (npr. resenja obrisanog studenta) i dalje rade.
 * Lombok @Data u entitetu generise ove dve metode iz polja "obrisan".
 */
public interface Obrisiv {

    boolean isObrisan();

    void setObrisan(boolean obrisan);
}
