package org.raflab.studentski_server.services;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.request.KorisnikRequest;
import org.raflab.studentski_server.controllers.response.KorisnikResponse;
import org.raflab.studentski_server.controllers.response.PageResponse;
import org.raflab.studentski_server.exceptions.BadRequestException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.Korisnik;
import org.raflab.studentski_server.model.Uloga;
import org.raflab.studentski_server.repositories.KorisnikRepository;
import org.raflab.studentski_server.repositories.NastavnikRepository;
import org.raflab.studentski_server.repositories.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * CRUD za naloge, napisan rucno (bez CrudService kalupa), kao kod profesora.
 * Nastavnik pravi nalog studentu ili drugom nastavniku.
 */
@Service
@AllArgsConstructor
public class KorisnikService {

    final KorisnikRepository korisnikRepository;
    final StudentRepository studentRepository;
    final NastavnikRepository nastavnikRepository;
    final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<KorisnikResponse> nadjiSve(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("page mora biti 0 ili vise, a size izmedju 1 i 100");
        }
        Page<Korisnik> stranica = korisnikRepository.findAllByObrisanFalse(PageRequest.of(page, size, Sort.by("id")));
        return PageResponse.of(stranica, this::toResponse);
    }

    @Transactional(readOnly = true)
    public KorisnikResponse nadji(Long id) {
        return toResponse(nadjiEntitet(id));
    }

    @Transactional
    public KorisnikResponse napravi(KorisnikRequest request) {
        Korisnik korisnik = new Korisnik();
        popuni(korisnik, request);
        // saveAndFlush odmah salje INSERT, pa duplikat emaila postaje 409 ovde
        return toResponse(korisnikRepository.saveAndFlush(korisnik));
    }

    // izmena uvek trazi lozinku, pa sluzi i kao reset lozinke
    @Transactional
    public KorisnikResponse izmeni(Long id, KorisnikRequest request) {
        Korisnik korisnik = nadjiEntitet(id);
        popuni(korisnik, request);
        return toResponse(korisnikRepository.saveAndFlush(korisnik));
    }

    // soft brisanje - obrisan korisnik vise ne moze da se uloguje
    @Transactional
    public void obrisi(Long id) {
        Korisnik korisnik = nadjiEntitet(id);
        korisnik.setObrisan(true);
        korisnikRepository.save(korisnik);
    }

    // ---------- pomocne ----------

    private Korisnik nadjiEntitet(Long id) {
        return korisnikRepository.findByIdAndObrisanFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik " + id + " ne postoji"));
    }

    // request -> entitet
    private void popuni(Korisnik korisnik, KorisnikRequest request) {
        korisnik.setEmail(request.email().trim());
        korisnik.setLozinka(passwordEncoder.encode(request.lozinka()));   // nikad cist tekst u bazi
        korisnik.setUloga(request.uloga());
        korisnik.setStudent(null);
        korisnik.setNastavnik(null);

        if (request.uloga() == Uloga.STUDENT) {
            if (request.studentId() == null) {
                throw new BadRequestException("Za ulogu STUDENT treba studentId");
            }
            korisnik.setStudent(studentRepository.findByIdAndObrisanFalse(request.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student " + request.studentId() + " ne postoji")));
        } else {
            if (request.nastavnikId() == null) {
                throw new BadRequestException("Za ulogu NASTAVNIK treba nastavnikId");
            }
            korisnik.setNastavnik(nastavnikRepository.findByIdAndObrisanFalse(request.nastavnikId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nastavnik " + request.nastavnikId() + " ne postoji")));
        }
    }

    // entitet -> DTO, bez lozinke
    private KorisnikResponse toResponse(Korisnik k) {
        return new KorisnikResponse(k.getId(), k.getEmail(), k.getUloga(),
                k.getStudent() != null ? k.getStudent().getId() : null,
                k.getNastavnik() != null ? k.getNastavnik().getId() : null);
    }
}
