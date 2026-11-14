package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Ispitni rok ili kolokvijum, npr. skolska godina 2026-2027, naziv Januar
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"skolskaGodina", "naziv"}))
public class IspitniRok implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skolskaGodina;   // 2026-2027

    @Column(nullable = false)
    private String naziv;           // Prvi kolokvijum, Januar...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipRoka tip;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
