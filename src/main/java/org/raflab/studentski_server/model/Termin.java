package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * Jedan ispit moze imati vise termina (termin 1 u 10h, termin 2 u 14h...)
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ispit_id", "redniBroj"}))
public class Termin implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Ispit ispit;

    private int redniBroj;

    @Column(nullable = false)
    private LocalDateTime pocetak;

    @Column(nullable = false)
    private LocalDateTime kraj;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
