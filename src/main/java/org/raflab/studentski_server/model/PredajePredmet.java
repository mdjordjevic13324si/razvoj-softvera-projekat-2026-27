package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Koje predmete nastavnik predaje - njima i upravlja (ispiti, zadaci, spiskovi, ocenjivanje)
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"nastavnik_id", "predmet_id"}))
public class PredajePredmet implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Nastavnik nastavnik;

    @ManyToOne(optional = false)
    private Predmet predmet;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
