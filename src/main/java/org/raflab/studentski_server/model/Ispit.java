package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/*
 * Predmet u jednom roku, npr. RS u januaru. Student prijavljuje ispit, a ne termin ili grupu.
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ispitniRok_id", "predmet_id"}))
public class Ispit implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private IspitniRok ispitniRok;

    @ManyToOne(optional = false)
    private Predmet predmet;

    @Column(nullable = false)
    private LocalDate datum;   // od njega se racuna rok za prijavu/odjavu

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
