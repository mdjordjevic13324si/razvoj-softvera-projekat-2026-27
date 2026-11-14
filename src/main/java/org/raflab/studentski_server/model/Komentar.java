package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * Komentar nastavnika na odredjenu liniju odredjenog fajla u resenju
 */
@Entity
@Data
public class Komentar implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Resenje resenje;

    @ManyToOne(optional = false)
    private Nastavnik nastavnik;

    @Column(nullable = false)
    private String putanjaFajla;   // putanja unutar projekta, npr. src/main/java/Main.java

    private int linija;

    @Column(nullable = false, length = 2000)
    private String tekst;

    @Column(nullable = false)
    private LocalDateTime vreme;

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
