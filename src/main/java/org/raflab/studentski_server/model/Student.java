package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Jedan student = jedan indeks. Indeks je smer + broj + godina upisa, npr. RN 12/2023
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"smer_id", "brojIndeksa", "godinaUpisa"}))
public class Student implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ime;

    @Column(nullable = false)
    private String prezime;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(optional = false)
    private Smer smer;

    private int brojIndeksa;
    private int godinaUpisa;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
