package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * Prijava studenta za ispit. Nema soft delete: odjava postavlja aktivna=false,
 * pa uvek postoji najvise jedan red po studentu i ispitu.
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "ispit_id"}))
public class PrijavaIspita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Ispit ispit;

    private boolean aktivna;

    @Column(nullable = false)
    private LocalDateTime vremePromene;
}
