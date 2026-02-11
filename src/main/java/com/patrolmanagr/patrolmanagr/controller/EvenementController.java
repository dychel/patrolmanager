package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.config.*;
import com.patrolmanagr.patrolmanagr.dto.EvenementDTO;
import com.patrolmanagr.patrolmanagr.entity.Evenement;
import com.patrolmanagr.patrolmanagr.service.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/patrolmanagr/evenements")
public class EvenementController {

    @Autowired
    private EvenementService evenementService;

    @PostMapping("/create")
    public ResponseEntity<Evenement> createEvenement(@RequestBody EvenementDTO evenementDTO) {
        Evenement evenement = evenementService.createEvenement(evenementDTO);
        return ResponseEntity.ok(evenement);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evenement> getEvenement(@PathVariable Long id) {
        Evenement evenement = evenementService.getEvenementById(id);
        return ResponseEntity.ok(evenement);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Evenement>> getAllEvenements() {
        List<Evenement> evenements = evenementService.getAllEvenements();
        return ResponseEntity.ok(evenements);
    }

    @GetMapping("/by-exec-ronde/{execRondeId}")
    public ResponseEntity<List<Evenement>> getEvenementsByExecRonde(@PathVariable Long execRondeId) {
        List<Evenement> evenements = evenementService.getEvenementsByExecRonde(execRondeId);
        return ResponseEntity.ok(evenements);
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<Evenement>> getEvenementsByType(@PathVariable EvenementType type) {
        List<Evenement> evenements = evenementService.getEvenementsByType(type);
        return ResponseEntity.ok(evenements);
    }

    @GetMapping("/by-severity/{severity}")
    public ResponseEntity<List<Evenement>> getEvenementsBySeverity(@PathVariable EvenementSeverity severity) {
        List<Evenement> evenements = evenementService.getEvenementsBySeverity(severity);
        return ResponseEntity.ok(evenements);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Evenement>> getEvenementsByStatus(@PathVariable EvenementStatus status) {
        List<Evenement> evenements = evenementService.getEvenementsByStatus(status);
        return ResponseEntity.ok(evenements);
    }

    @GetMapping("/eleves/actifs")
    public ResponseEntity<List<Evenement>> getActiveHighSeverityEvents() {
        List<Evenement> evenements = evenementService.getActiveCriticalEvents();
        return ResponseEntity.ok(evenements);
    }

    @PutMapping("/{id}/traiter")
    public ResponseEntity<Evenement> resolveEvenement(
            @PathVariable Long id,
            @RequestParam String resolutionNotes) {
        Evenement evenement = evenementService.resolveEvenement(id, resolutionNotes);
        return ResponseEntity.ok(evenement);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evenement> updateEvenement(
            @PathVariable Long id,
            @RequestBody EvenementDTO evenementDTO) {
        Evenement evenement = evenementService.updateEvenement(id, evenementDTO);
        return ResponseEntity.ok(evenement);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvenement(@PathVariable Long id) {
        evenementService.deleteEvenement(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/site/{siteId}")
    public ResponseEntity<Long> getRecentEventCount(
            @PathVariable Long siteId,
            @RequestParam(defaultValue = "24") int hours) {
        Long count = evenementService.countRecentEventsBySite(siteId, hours);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Evenement>> searchEvenements(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long execRondeId,
            @RequestParam(required = false) EvenementType type,
            @RequestParam(required = false) EvenementSeverity severity,
            @RequestParam(required = false) EvenementStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Evenement> evenements = evenementService.searchEvenements(
                siteId, execRondeId, type, severity, status, startDate, endDate);
        return ResponseEntity.ok(evenements);
    }
}