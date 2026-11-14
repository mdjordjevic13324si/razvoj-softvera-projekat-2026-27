package org.raflab.studentski_server.model;

import jakarta.persistence.*;
import lombok.Data;

/*
 * Zadatak (zip projekta) za jednu grupu. Zip je na disku, u bazi je samo relativna putanja.
 */
@Entity
@Data
public class Zadatak implements Obrisiv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true)
    private Grupa grupa;           // jedan zadatak po grupi

    @ManyToOne(optional = false)
    private Nastavnik nastavnik;   // ko ga je postavio

    @Column(nullable = false)
    private String naslov;

    @Column(length = 2000)
    private String opis;

    @Column(nullable = false)
    private String putanjaZipa;    // npr. 2026-2027/januar/RS/zadaci/termin-1/grupa-A.zip

    private String originalnoIme;  // ime fajla koje je poslao klijent

    private boolean obrisan;   // soft delete - vidi Obrisiv
}
