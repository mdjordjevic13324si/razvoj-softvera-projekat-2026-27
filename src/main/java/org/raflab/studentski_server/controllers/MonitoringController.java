package org.raflab.studentski_server.controllers;

import lombok.AllArgsConstructor;
import org.raflab.studentski_server.controllers.response.MonitoringGrupeResponse;
import org.raflab.studentski_server.controllers.response.MonitoringStudentaResponse;
import org.raflab.studentski_server.controllers.response.MonitoringTerminaResponse;
import org.raflab.studentski_server.services.MonitoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Podaci za veb aplikaciju za monitoring - samo nastavnik (pravilo je u SecurityConfig)
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/monitoring")
public class MonitoringController {

    final MonitoringService monitoringService;

    // ko je preuzeo i ko je predao u jednoj grupi
    @GetMapping(path = "/grupe/{grupaId}")
    public MonitoringGrupeResponse grupa(@PathVariable("grupaId") Long grupaId) {
        return monitoringService.grupa(grupaId);
    }

    // sve grupe jednog termina, sa zbirom za ceo termin
    @GetMapping(path = "/termini/{terminId}")
    public MonitoringTerminaResponse termin(@PathVariable("terminId") Long terminId) {
        return monitoringService.termin(terminId);
    }

    // sva polaganja jednog studenta
    @GetMapping(path = "/studenti/{studentId}")
    public MonitoringStudentaResponse student(@PathVariable("studentId") Long studentId) {
        return monitoringService.student(studentId);
    }
}
