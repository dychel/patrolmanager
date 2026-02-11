package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.config.IncidentSeverity;
import com.patrolmanagr.patrolmanagr.config.IncidentStatus;
import com.patrolmanagr.patrolmanagr.config.IncidentType;
import com.patrolmanagr.patrolmanagr.dto.IncidentDTO;
import com.patrolmanagr.patrolmanagr.entity.Incident;
import com.patrolmanagr.patrolmanagr.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patrolmanagr/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @PostMapping("/create")
    public ResponseEntity<Incident> createIncident(@RequestBody IncidentDTO incidentDTO) {
        Incident incident = incidentService.createIncident(incidentDTO);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> getIncident(@PathVariable Long id) {
        Incident incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Incident>> getAllIncidents() {
        List<Incident> incidents = incidentService.getAllIncidents();
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/by-exec-ronde/{execRondeId}")
    public ResponseEntity<List<Incident>> getIncidentsByExecRonde(@PathVariable Long execRondeId) {
        List<Incident> incidents = incidentService.getIncidentsByExecRonde(execRondeId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/by-site/{siteId}")
    public ResponseEntity<List<Incident>> getIncidentsBySite(@PathVariable Long siteId) {
        List<Incident> incidents = incidentService.getIncidentsBySite(siteId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<Incident>> getIncidentsByType(@PathVariable IncidentType type) {
        List<Incident> incidents = incidentService.getIncidentsByType(type);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Incident>> getIncidentsByStatus(@PathVariable IncidentStatus status) {
        List<Incident> incidents = incidentService.getIncidentsByStatus(status);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/prioritaires")
    public ResponseEntity<List<Incident>> getPriorityIncidents() {
        List<Incident> incidents = incidentService.getPriorityIncidents();
        return ResponseEntity.ok(incidents);
    }

    @PutMapping("/{id}/resoudre")
    public ResponseEntity<Incident> resolveIncident(
            @PathVariable Long id,
            @RequestParam String resolutionNotes) {
        Incident incident = incidentService.resolveIncident(id, resolutionNotes);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/fermer")
    public ResponseEntity<Incident> closeIncident(@PathVariable Long id) {
        Incident incident = incidentService.closeIncident(id);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/en-cours")
    public ResponseEntity<Incident> startProcessingIncident(@PathVariable Long id) {
        Incident incident = incidentService.startProcessingIncident(id);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/reouvrir")
    public ResponseEntity<Incident> reopenIncident(
            @PathVariable Long id,
            @RequestParam String reason) {
        Incident incident = incidentService.reopenIncident(id, reason);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/update-severite")
    public ResponseEntity<Incident> updateSeverity(
            @PathVariable Long id,
            @RequestParam IncidentSeverity severity) {
        Incident incident = incidentService.updateSeverity(id, severity);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}/assigner")
    public ResponseEntity<Incident> assignAgent(
            @PathVariable Long id,
            @RequestParam Long agentUserId) {
        Incident incident = incidentService.assignAgent(id, agentUserId);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIncidentStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> stats = incidentService.getIncidentStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/site/{siteId}")
    public ResponseEntity<Map<String, Object>> getIncidentStatsBySite(
            @PathVariable Long siteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> stats = incidentService.getIncidentStatsBySite(siteId, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Incident>> searchIncidents(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long rondeId,
            @RequestParam(required = false) IncidentType type,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Incident> incidents = incidentService.searchIncidents(siteId, rondeId, type, status, startDate, endDate);
        return ResponseEntity.ok(incidents);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> updateIncident(
            @PathVariable Long id,
            @RequestBody IncidentDTO incidentDTO) {
        Incident incident = incidentService.updateIncident(id, incidentDTO);
        return ResponseEntity.ok(incident);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.ok().build();
    }
}