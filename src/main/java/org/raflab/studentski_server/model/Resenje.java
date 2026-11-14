package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * Nastaje kad student preuzme zadatak, a popunjava se kad ga preda.
 * Iz ovih vremena se racunaju podaci za monitoring.
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "zadatak_id"}))
public class Resenje implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Zadatak zadatak;

    @Column(nullable = false)
    private LocalDateTime vremePreuzimanja;   // prvo preuzimanje

    private LocalDateTime vremePredaje;       // poslednja predaja, null dok ne preda

    private String putanjaZipa;               // null dok ne preda

    private Integer poeni;                    // null dok nije ocenjeno

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
