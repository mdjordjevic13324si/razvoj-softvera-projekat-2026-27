package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Sve ucionice (RAF, Kolarac...) su u jednoj tabeli, adresa se ponavlja
 */
@Entity
@Data
public class Ucionica implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String oznaka;   // RAF1, Kolarac 3...

    @Column(nullable = false)
    private String adresa;

    private int sprat;
    private int kapacitet;
    private int brojRacunara;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
