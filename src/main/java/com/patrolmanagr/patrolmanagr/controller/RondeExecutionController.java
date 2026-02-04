package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.entity.SysJobRun;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RondeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/execution/*")
public class RondeExecutionController {

    @Autowired
    private RondeExecutionService rondeExecutionService;

    @PostMapping("/execute-job/{jobId}")
    public ResponseEntity<ResponseMessage> executeJob(@PathVariable Long jobId) {
        SysJobRun jobRun = rondeExecutionService.executeJob(jobId);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job exécuté avec succès", jobRun),
                HttpStatus.OK
        );
    }

    @PostMapping("/execute-job-manual/{jobId}")
    public ResponseEntity<ResponseMessage> executeJobManually(@PathVariable Long jobId) {
        SysJobRun jobRun = rondeExecutionService.executeJobManually(jobId);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job exécuté manuellement avec succès", jobRun),
                HttpStatus.OK
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<ResponseMessage> getExecutionStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> stats = rondeExecutionService.getExecutionStats(startDate, endDate);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Statistiques d'exécution", stats),
                HttpStatus.OK
        );
    }

    @GetMapping("/test-pointage/{siteId}")
    public ResponseEntity<ResponseMessage> testPointageAnalysis(@PathVariable Long siteId) {
        // Cette méthode est pour tester l'analyse des pointages
        // Vous pouvez l'utiliser pour déboguer
        return new ResponseEntity<>(
                new ResponseMessage("success", "Test endpoint",
                        "L'analyse des pointages est fonctionnelle pour le site " + siteId),
                HttpStatus.OK
        );
    }
}