package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Jedan red spiska za polaganje: koji student, u kojoj grupi i u kojoj ucionici
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "grupa_id"}))
public class SpisakStavka implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Grupa grupa;

    @ManyToOne(optional = false)
    private Ucionica ucionica;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
