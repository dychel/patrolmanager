package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.dto.ExecRondeDTO;
import com.patrolmanagr.patrolmanagr.dto.RondeExecutionReportDTO;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RondeExecutionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/executed-rondes/*")
public class ExecutedRondesController {

    @Autowired
    private RondeExecutionQueryService rondeExecutionQueryService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllExecutedRondes() {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getAllExecutedRondes();
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Liste de toutes les rondes exécutées (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/today")
    public ResponseEntity<ResponseMessage> getTodayExecutedRondes() {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getTodayExecutedRondes();
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Rondes exécutées aujourd'hui (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/in-progress")
    public ResponseEntity<ResponseMessage> getInProgressRondes() {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getInProgressRondes();
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Rondes en cours d'exécution (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/completed")
    public ResponseEntity<ResponseMessage> getCompletedRondes() {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getCompletedRondes();
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Rondes terminées (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/by-date/{date}")
    public ResponseEntity<ResponseMessage> getExecutedRondesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getExecutedRondesByDate(date);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Rondes exécutées le " + date + " (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/by-site/{siteId}/{date}")
    public ResponseEntity<ResponseMessage> getExecutedRondesBySiteAndDate(
            @PathVariable Long siteId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getExecutedRondesBySiteAndDate(siteId, date);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Rondes exécutées pour le site " + siteId + " le " + date + " (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/by-site-period/{siteId}")
    public ResponseEntity<ResponseMessage> getExecutedRondesBySiteAndPeriod(
            @PathVariable Long siteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getExecutedRondesBySiteAndPeriod(siteId, startDate, endDate);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Rondes exécutées pour le site " + siteId + " du " + startDate + " au " + endDate + " (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<ResponseMessage> getExecutedRondesByStatus(@PathVariable String status) {
        try {
            Status_exec_Ronde statusEnum = Status_exec_Ronde.valueOf(status.toUpperCase());
            List<ExecRondeDTO> rondes = rondeExecutionQueryService.getExecutedRondesByStatus(statusEnum);
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Rondes exécutées avec statut " + status + " (" + rondes.size() + ")",
                            rondes),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Statut invalide. Valeurs acceptées: PLANNED, IN_PROGRESS, DONE, CANCELLED",
                            null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/by-ronde/{rondeId}")
    public ResponseEntity<ResponseMessage> getExecutedRondesForRonde(@PathVariable Long rondeId) {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getExecutedRondesForRonde(rondeId);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Historique des exécutions pour la ronde " + rondeId + " (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/details/{execRondeId}")
    public ResponseEntity<ResponseMessage> getRondeExecutionDetails(@PathVariable Long execRondeId) {
        RondeExecutionReportDTO report = rondeExecutionQueryService.getRondeExecutionDetails(execRondeId);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Détails de l'exécution de ronde",
                        report),
                HttpStatus.OK
        );
    }

    @GetMapping("/history/{rondeId}")
    public ResponseEntity<ResponseMessage> getRondeExecutionHistory(@PathVariable Long rondeId) {
        List<RondeExecutionReportDTO> history = rondeExecutionQueryService.getRondeExecutionHistory(rondeId);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Historique complet des exécutions pour la ronde " + rondeId + " (" + history.size() + " exécutions)",
                        history),
                HttpStatus.OK
        );
    }

    @GetMapping("/by-job-run/{jobRunId}")
    public ResponseEntity<ResponseMessage> getExecutedRondesByJobRun(@PathVariable Long jobRunId) {
        List<ExecRondeDTO> rondes = rondeExecutionQueryService.getExecutedRondesByJobRun(jobRunId);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Rondes exécutées par le job run " + jobRunId + " (" + rondes.size() + ")",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseMessage> searchExecutedRondes(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long rondeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {

        Status_exec_Ronde statusEnum = null;
        if (status != null) {
            try {
                statusEnum = Status_exec_Ronde.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(
                        new ResponseMessage("error",
                                "Statut invalide. Valeurs acceptées: PLANNED, IN_PROGRESS, DONE, CANCELLED",
                                null),
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        List<ExecRondeDTO> rondes = rondeExecutionQueryService.searchExecutedRondes(
                siteId, rondeId, startDate, endDate, statusEnum);

        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Résultats de la recherche (" + rondes.size() + " rondes trouvées)",
                        rondes),
                HttpStatus.OK
        );
    }

    @GetMapping("/site-stats/{siteId}")
    public ResponseEntity<ResponseMessage> getSiteExecutionStats(
            @PathVariable Long siteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> stats = rondeExecutionQueryService.getSiteExecutionStats(siteId, startDate, endDate);
        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Statistiques d'exécution pour le site " + siteId,
                        stats),
                HttpStatus.OK
        );
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<ResponseMessage> getDashboardSummary() {
        LocalDate today = LocalDate.now();

        Map<String, Object> summary = new java.util.HashMap<>();

        // Rondes exécutées aujourd'hui
        List<ExecRondeDTO> todayRondes = rondeExecutionQueryService.getTodayExecutedRondes();
        summary.put("todayRondes", todayRondes.size());

        // Rondes en cours
        List<ExecRondeDTO> inProgressRondes = rondeExecutionQueryService.getInProgressRondes();
        summary.put("inProgressRondes", inProgressRondes.size());

        // Rondes terminées aujourd'hui
        long completedToday = todayRondes.stream()
                .filter(r -> r.getStatus() == Status_exec_Ronde.DONE)
                .count();
        summary.put("completedToday", completedToday);

        // Incidents aujourd'hui
        long todayIncidents = todayRondes.stream()
                .mapToInt(ExecRondeDTO::getIncidentCount)
                .sum();
        summary.put("todayIncidents", todayIncidents);

        // Taux de complétion moyen aujourd'hui
        double avgCompletionToday = todayRondes.stream()
                .filter(r -> r.getCompletionRate() != null)
                .mapToDouble(r -> r.getCompletionRate().doubleValue())
                .average()
                .orElse(0.0);
        summary.put("avgCompletionToday", avgCompletionToday);

        // Sites avec des problèmes aujourd'hui
        List<String> sitesWithIssues = todayRondes.stream()
                .filter(r -> r.getIncidentCount() > 0)
                .map(ExecRondeDTO::getSiteName)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        summary.put("sitesWithIssues", sitesWithIssues);
        summary.put("sitesWithIssuesCount", sitesWithIssues.size());

        return new ResponseEntity<>(
                new ResponseMessage("success",
                        "Résumé du tableau de bord",
                        summary),
                HttpStatus.OK
        );
    }
}