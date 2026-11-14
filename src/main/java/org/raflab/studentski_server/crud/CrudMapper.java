package org.raflab.studentski_server.crud;

/*
 * Ugovor koji svaki entitet ispunjava da bi dobio genericki CRUD.
 * Kalup (CrudService/CrudController) ne zna nista o konkretnom entitetu -
 * sve sto mu treba da zna, saznaje preko ovih metoda.
 *
 * Jedan response DTO za sve uloge, kao kod profesora. Ko sme na koji endpoint
 * i ciji podaci se vide resava se u SecurityConfig i @PreAuthorize, ne ovde.
 *
 * ENTITY   - JPA entitet, npr. Ucionica
 * REQUEST  - sta klijent salje za create i update, npr. UcionicaRequest
 * RESPONSE - sta klijent dobija nazad, npr. UcionicaResponse
 */
public interface CrudMapper<ENTITY, REQUEST, RESPONSE> {

    // create: od zahteva pravi NOV entitet (bez id-a, id dodeljuje baza)
    ENTITY createEntityFromRequest(REQUEST request);

    // update: prepisuje polja POSTOJECEG entiteta vrednostima iz zahteva (id ostaje isti)
    void updateExistingEntityFromRequest(ENTITY existingEntity, REQUEST request);

    // odgovor koji ide klijentu - entitet nikad ne izlazi iz servera
    RESPONSE toResponse(ENTITY entity);
}
