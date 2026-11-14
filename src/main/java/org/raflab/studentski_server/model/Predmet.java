package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Predmet implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sifra;   // koristi se i za ime foldera, npr. RS

    @Column(nullable = false)
    private String naziv;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
