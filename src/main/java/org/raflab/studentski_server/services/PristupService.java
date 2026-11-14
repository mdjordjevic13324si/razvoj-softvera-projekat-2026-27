package org.raflab.studentski_server.services;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.exceptions.ForbiddenException;
import org.raflab.studentski_server.exceptions.UnauthorizedException;
import org.raflab.studentski_server.model.Nastavnik;
import org.raflab.studentski_server.model.Predmet;
import org.raflab.studentski_server.model.Resenje;
import org.raflab.studentski_server.model.Student;
import org.raflab.studentski_server.repositories.NastavnikRepository;
import org.raflab.studentski_server.repositories.PredajePredmetRepository;
import org.raflab.studentski_server.repositories.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * Ko je prijavljen (iz JWT tokena) i provere vlasnistva.
 */
@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class PristupService {

    final NastavnikRepository nastavnikRepository;
    final PredajePredmetRepository predajePredmetRepository;
    final StudentRepository studentRepository;

    // Student id iz tokena, null ako prijavljeni korisnik nije student
    public Long ulogovaniStudentId() {
        return idIzTokena(AuthService.CLAIM_STUDENT_ID);
    }

    // Nastavnik id iz tokena, null ako prijavljeni korisnik nije nastavnik
    public Long ulogovaniNastavnikId() {
        return idIzTokena(AuthService.CLAIM_NASTAVNIK_ID);
    }

    // vraca prijavljenog studenta; 403 ako prijavljeni korisnik nije student
    public Student ulogovaniStudent() {
        Long studentId = ulogovaniStudentId();
        if (studentId == null) {
            throw new ForbiddenException("Ova akcija je dozvoljena samo studentu");
        }
        return studentRepository.findByIdAndObrisanFalse(studentId)
                .orElseThrow(() -> new UnauthorizedException("Nalog studenta vise ne postoji"));
    }

    /*
     * Vraca prijavljenog nastavnika ako predaje dati predmet, inace baca 403.
     * Nastavnik upravlja (zadaci, spiskovi, ocenjivanje) samo predmetima koje predaje.
     */
    public Nastavnik ulogovaniNastavnikKojiPredaje(Predmet predmet) {
        Long nastavnikId = ulogovaniNastavnikId();
        if (nastavnikId == null) {
            throw new ForbiddenException("Ova akcija je dozvoljena samo nastavniku");
        }
        Nastavnik nastavnik = nastavnikRepository.findByIdAndObrisanFalse(nastavnikId)
                .orElseThrow(() -> new UnauthorizedException("Nalog nastavnika vise ne postoji"));
        if (!predajePredmetRepository.existsByNastavnikIdAndPredmetIdAndObrisanFalse(nastavnikId, predmet.getId())) {
            throw new ForbiddenException("Ne predajete predmet " + predmet.getSifra());
        }
        return nastavnik;
    }

    // da li resenje pripada prijavljenom studentu
    public boolean jeSvojeResenje(Resenje resenje) {
        Long studentId = ulogovaniStudentId();
        return studentId != null && studentId.equals(resenje.getStudent().getId());
    }

    /*
     * Ko sme da vidi resenje sa poenima i komentarima:
     * student samo svoje, a nastavnik samo iz predmeta koji predaje. Inace 403.
     */
    public void proveriCitanjeResenja(Resenje resenje) {
        if (ulogovaniStudentId() != null) {
            if (!jeSvojeResenje(resenje)) {
                throw new ForbiddenException("Mozete videti samo svoje resenje");
            }
            return;
        }
        ulogovaniNastavnikKojiPredaje(resenje.getZadatak().getGrupa().getTermin().getIspit().getPredmet());
    }

    private Long idIzTokena(String nazivClaima) {
        Jwt jwt = ulogovaniJwt();
        if (jwt == null) return null;
        Object id = jwt.getClaim(nazivClaima);
        return id instanceof Number broj ? broj.longValue() : null;
    }

    private Jwt ulogovaniJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Jwt jwt ? jwt : null;
    }
}
