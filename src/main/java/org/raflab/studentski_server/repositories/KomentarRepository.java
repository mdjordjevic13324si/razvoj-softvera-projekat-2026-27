package org.raflab.studentski_server.repositories;

import org.raflab.studentski_server.model.Komentar;

import java.util.List;

public interface KomentarRepository extends ObrisivRepository<Komentar> {

    // komentari jednog resenja, redom po fajlu pa po liniji
    List<Komentar> findAllByResenjeIdAndObrisanFalseOrderByPutanjaFajlaAscLinijaAscIdAsc(Long resenjeId);
}
