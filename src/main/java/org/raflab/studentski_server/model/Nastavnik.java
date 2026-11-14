package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Nastavnik implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ime;

    @Column(nullable = false)
    private String prezime;

    @Column(nullable = false, unique = true)
    private String email;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
