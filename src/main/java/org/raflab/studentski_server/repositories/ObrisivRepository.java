package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Obrisiv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/*
 * Zajednicki repozitorijum za sve entitete sa soft delete-om.
 * Spring Data iz imena metoda sam pravi upite sa "where obrisan = false",
 * posebno za svaki entitet koji ga nasledi.
 *
 * @NoRepositoryBean - ovo nije repozitorijum za sebe, samo osnova za druge.
 */
@NoRepositoryBean
public interface ObrisivRepository<ENTITY extends Obrisiv> extends JpaRepository<ENTITY, Long> {

    // "daj sve" u CRUD-u - samo neobrisani, po stranicama
    Page<ENTITY> findAllByObrisanFalse(Pageable pageable);

    // "daj po id-ju" u CRUD-u - obrisan se ponasa kao da ne postoji
    Optional<ENTITY> findByIdAndObrisanFalse(Long id);
}
