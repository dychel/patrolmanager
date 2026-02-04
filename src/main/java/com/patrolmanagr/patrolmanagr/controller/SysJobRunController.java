package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.config.JobRunStatus;
import com.patrolmanagr.patrolmanagr.dto.SysJobRunDTO;
import com.patrolmanagr.patrolmanagr.entity.SysJobRun;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.SysJobRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/sys-job-runs/*")
public class SysJobRunController {

    @Autowired
    private SysJobRunService sysJobRunService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage> createJobRun(@Valid @RequestBody SysJobRunDTO jobRunDTO) {
        SysJobRun jobRun = sysJobRunService.createJobRun(jobRunDTO);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job run créé avec succès", jobRun),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/start/{jobId}")
    public ResponseEntity<ResponseMessage> startJobRun(@PathVariable Long jobId) {
        SysJobRun jobRun = sysJobRunService.startJobRun(jobId);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job run démarré", jobRun),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{runId}/update")
    public ResponseEntity<ResponseMessage> updateJobRun(
            @PathVariable Long runId,
            @RequestParam String status,
            @RequestParam(required = false) String message) {

        JobRunStatus jobRunStatus;
        try {
            jobRunStatus = JobRunStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error", "Status invalide. Valeurs acceptées: RUNNING, SUCCESS, PARTIAL_SUCCESS, ERROR, CANCELLED", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        SysJobRun jobRun = sysJobRunService.updateJobRun(runId, jobRunStatus, message);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job run mis à jour", jobRun),
                HttpStatus.OK
        );
    }

    @PutMapping("/{runId}/complete")
    public ResponseEntity<ResponseMessage> completeJobRun(
            @PathVariable Long runId,
            @RequestParam String status,
            @RequestParam(required = false) String message) {

        JobRunStatus jobRunStatus;
        try {
            jobRunStatus = JobRunStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error", "Status invalide. Valeurs acceptées: RUNNING, SUCCESS, PARTIAL_SUCCESS, ERROR, CANCELLED", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        SysJobRun jobRun = sysJobRunService.completeJobRun(runId, jobRunStatus, message);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job run terminé", jobRun),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getJobRunById(@PathVariable Long id) {
        SysJobRun jobRun = sysJobRunService.getJobRunById(id);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job run trouvé", jobRun),
                HttpStatus.OK
        );
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ResponseMessage> getJobRunsByJobId(@PathVariable Long jobId) {
        List<SysJobRun> jobRuns = sysJobRunService.getJobRunsByJobId(jobId);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job runs pour le job " + jobId, jobRuns),
                HttpStatus.OK
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseMessage> getJobRunsByStatus(@PathVariable String status) {
        List<SysJobRun> jobRuns = sysJobRunService.getJobRunsByStatus(status);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job runs avec status " + status, jobRuns),
                HttpStatus.OK
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<ResponseMessage> getJobRunsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<SysJobRun> jobRuns = sysJobRunService.getJobRunsByDateRange(startDate, endDate);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job runs dans la plage de dates", jobRuns),
                HttpStatus.OK
        );
    }

    @GetMapping("/running")
    public ResponseEntity<ResponseMessage> getRunningJobs() {
        List<SysJobRun> jobRuns = sysJobRunService.getRunningJobs();
        return new ResponseEntity<>(
                new ResponseMessage("success", "Jobs en cours d'exécution", jobRuns),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/clean-old/{daysToKeep}")
    public ResponseEntity<ResponseMessage> cleanOldRuns(@PathVariable int daysToKeep) {
        sysJobRunService.cleanOldRuns(daysToKeep);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Anciens job runs supprimés (conservés: " + daysToKeep + " jours)", null),
                HttpStatus.OK
        );
    }

    @GetMapping("/latest/job/{jobId}")
    public ResponseEntity<ResponseMessage> getLatestJobRun(@PathVariable Long jobId) {
        SysJobRun jobRun = sysJobRunService.getLatestJobRun(jobId);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Dernier job run", jobRun),
                HttpStatus.OK
        );
    }
}