package org.raflab.studentski_server.services;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.response.*;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.raflab.studentski_server.model.*;
import org.raflab.studentski_server.repositories.*;
import org.raflab.studentski_server.utils.Converters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Podaci za veb aplikaciju za monitoring: ko je preuzeo zadatak, ko je predao resenje i koliko je radio.
 * Samo cita podatke koje su upisale stavke 4 (preuzimanje) i 5 (predaja).
 * Veb aplikacija "uzivo" prikaz dobija tako sto ove endpointe poziva na svakih nekoliko sekundi.
 */
@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class MonitoringService {

    final GrupaRepository grupaRepository;
    final TerminRepository terminRepository;
    final StudentRepository studentRepository;
    final SpisakStavkaRepository spisakStavkaRepository;
    final ZadatakRepository zadatakRepository;
    final ResenjeRepository resenjeRepository;

    // ---------- po grupi ----------

    public MonitoringGrupeResponse grupa(Long grupaId) {
        Grupa grupa = grupaRepository.findByIdAndObrisanFalse(grupaId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupa " + grupaId + " ne postoji"));
        return monitoringGrupe(grupa);
    }

    // ---------- po terminu ----------

    public MonitoringTerminaResponse termin(Long terminId) {
        Termin termin = terminRepository.findByIdAndObrisanFalse(terminId)
                .orElseThrow(() -> new ResourceNotFoundException("Termin " + terminId + " ne postoji"));
        List<MonitoringGrupeResponse> grupe = grupaRepository.findAllByTerminIdAndObrisanFalseOrderByOznaka(terminId).stream()
                .map(this::monitoringGrupe)
                .toList();
        List<MonitoringStavkaResponse> sviStudenti = grupe.stream()
                .flatMap(grupa -> grupa.studenti().stream())
                .toList();
        Ispit ispit = termin.getIspit();

        return new MonitoringTerminaResponse(
                termin.getId(), termin.getRedniBroj(),
                ispit.getPredmet().getSifra(), ispit.getPredmet().getNaziv(), ispit.getIspitniRok().getNaziv(),
                termin.getPocetak(), termin.getKraj(),
                PolaganjeService.stanje(termin, LocalDateTime.now()),
                sviStudenti.size(), brojPreuzelih(sviStudenti), brojPredalih(sviStudenti),
                prosecnoVremeIzrade(sviStudenti),
                grupe);
    }

    // ---------- po studentu ----------

    public MonitoringStudentaResponse student(Long studentId) {
        Student student = studentRepository.findByIdAndObrisanFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + studentId + " ne postoji"));
        List<MonitoringPolaganjaResponse> polaganja = spisakStavkaRepository.findAllByStudentIdAndObrisanFalse(studentId).stream()
                .sorted(Comparator.comparing((SpisakStavka stavka) -> stavka.getGrupa().getTermin().getPocetak()))
                .map(this::monitoringPolaganja)
                .toList();
        return new MonitoringStudentaResponse(student.getId(), student.getIme(), student.getPrezime(),
                Converters.indeks(student), polaganja);
    }

    // ---------- pomocne metode ----------

    private MonitoringGrupeResponse monitoringGrupe(Grupa grupa) {
        Termin termin = grupa.getTermin();
        Ispit ispit = termin.getIspit();
        Optional<Zadatak> zadatak = zadatakGrupe(grupa.getId());

        // resenja zadatka po id-ju studenta, da se za svakog studenta sa spiska ne ide posebno u bazu
        Map<Long, Resenje> resenjePoStudentu = zadatak
                .map(z -> resenjeRepository.findAllByZadatakIdAndObrisanFalse(z.getId()))
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(resenje -> resenje.getStudent().getId(), Function.identity()));

        List<MonitoringStavkaResponse> studenti = spisakStavkaRepository.findAllByGrupaIdAndObrisanFalse(grupa.getId()).stream()
                .sorted(Comparator.comparing((SpisakStavka stavka) -> stavka.getStudent().getPrezime())
                        .thenComparing(stavka -> stavka.getStudent().getIme())
                        .thenComparing(stavka -> stavka.getStudent().getId()))
                .map(stavka -> monitoringStavke(stavka, resenjePoStudentu.get(stavka.getStudent().getId())))
                .toList();

        return new MonitoringGrupeResponse(
                grupa.getId(), grupa.getOznaka(),
                termin.getId(), termin.getRedniBroj(),
                ispit.getPredmet().getSifra(), ispit.getPredmet().getNaziv(), ispit.getIspitniRok().getNaziv(),
                termin.getPocetak(), termin.getKraj(),
                PolaganjeService.stanje(termin, LocalDateTime.now()),
                zadatak.isPresent(),
                studenti.size(), brojPreuzelih(studenti), brojPredalih(studenti),
                prosecnoVremeIzrade(studenti),
                studenti);
    }

    // resenje je null ako student jos nije preuzeo zadatak
    private MonitoringStavkaResponse monitoringStavke(SpisakStavka stavka, Resenje resenje) {
        Student student = stavka.getStudent();
        return new MonitoringStavkaResponse(
                student.getId(), student.getIme(), student.getPrezime(), Converters.indeks(student),
                stavka.getUcionica().getOznaka(),
                status(resenje),
                resenje == null ? null : resenje.getVremePreuzimanja(),
                resenje == null ? null : resenje.getVremePredaje(),
                vremeIzradeSekundi(resenje));
    }

    private MonitoringPolaganjaResponse monitoringPolaganja(SpisakStavka stavka) {
        Grupa grupa = stavka.getGrupa();
        Termin termin = grupa.getTermin();
        Ispit ispit = termin.getIspit();
        Resenje resenje = zadatakGrupe(grupa.getId())
                .flatMap(zadatak -> resenjeRepository.findByStudentIdAndZadatakId(stavka.getStudent().getId(), zadatak.getId()))
                .filter(r -> !r.isObrisan())
                .orElse(null);

        return new MonitoringPolaganjaResponse(
                grupa.getId(), grupa.getOznaka(),
                termin.getId(), termin.getRedniBroj(),
                ispit.getPredmet().getSifra(), ispit.getIspitniRok().getNaziv(),
                stavka.getUcionica().getOznaka(),
                termin.getPocetak(), termin.getKraj(),
                status(resenje),
                resenje == null ? null : resenje.getVremePreuzimanja(),
                resenje == null ? null : resenje.getVremePredaje(),
                vremeIzradeSekundi(resenje));
    }

    private Optional<Zadatak> zadatakGrupe(Long grupaId) {
        return zadatakRepository.findByGrupaId(grupaId).filter(zadatak -> !zadatak.isObrisan());
    }

    static StatusPolaganja status(Resenje resenje) {
        if (resenje == null) return StatusPolaganja.NIJE_PREUZEO;
        if (resenje.getVremePredaje() == null) return StatusPolaganja.PREUZEO;
        return StatusPolaganja.PREDAO;
    }

    // od preuzimanja do predaje; null dok student ne preda
    static Long vremeIzradeSekundi(Resenje resenje) {
        if (resenje == null || resenje.getVremePredaje() == null) return null;
        return Duration.between(resenje.getVremePreuzimanja(), resenje.getVremePredaje()).getSeconds();
    }

    static int brojPreuzelih(List<MonitoringStavkaResponse> studenti) {
        return (int) studenti.stream().filter(student -> student.status() != StatusPolaganja.NIJE_PREUZEO).count();
    }

    static int brojPredalih(List<MonitoringStavkaResponse> studenti) {
        return (int) studenti.stream().filter(student -> student.status() == StatusPolaganja.PREDAO).count();
    }

    // prosek za studente koji su predali, zaokruzen na sekundu; null ako niko nije predao
    static Long prosecnoVremeIzrade(List<MonitoringStavkaResponse> studenti) {
        List<Long> vremena = studenti.stream()
                .map(MonitoringStavkaResponse::vremeIzradeSekundi)
                .filter(Objects::nonNull)
                .toList();
        if (vremena.isEmpty()) return null;
        return Math.round(vremena.stream().mapToLong(Long::longValue).average().orElse(0));
    }
}
