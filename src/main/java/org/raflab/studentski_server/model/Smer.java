package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Studijski program, npr. RN - Racunarske nauke
 */
@Entity
@Data
public class Smer implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String oznaka;   // RN, RI, SI...

    @Column(nullable = false)
    private String naziv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VrstaSmera vrsta;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
