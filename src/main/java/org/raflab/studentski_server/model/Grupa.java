package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Grupa unutar termina (A, B, C...), svaka grupa ima svoj zadatak
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"termin_id", "oznaka"}))
public class Grupa implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Termin termin;

    @Column(nullable = false)
    private String oznaka;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
