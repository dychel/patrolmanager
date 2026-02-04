package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.SysJobDTO;
import com.patrolmanagr.patrolmanagr.entity.SysJob;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.SysJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/sys-jobs/*")
public class SysJobController {

    @Autowired
    private SysJobService sysJobService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage> createJob(@Valid @RequestBody SysJobDTO jobDTO) {
        SysJob job = sysJobService.createJob(jobDTO);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job créé avec succès", job),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseMessage> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody SysJobDTO jobDTO) {
        SysJob job = sysJobService.updateJob(id, jobDTO);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job mis à jour avec succès", job),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deleteJob(@PathVariable Long id) {
        sysJobService.deleteJob(id);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job supprimé avec succès", null),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getJobById(@PathVariable Long id) {
        SysJob job = sysJobService.getJobById(id);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job trouvé", job),
                HttpStatus.OK
        );
    }

    @GetMapping("/code/{jobCode}")
    public ResponseEntity<ResponseMessage> getJobByCode(@PathVariable String jobCode) {
        SysJob job = sysJobService.getJobByCode(jobCode);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Job trouvé", job),
                HttpStatus.OK
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllJobs() {
        List<SysJob> jobs = sysJobService.getAllJobs();
        return new ResponseEntity<>(
                new ResponseMessage("success", "Liste de tous les jobs", jobs),
                HttpStatus.OK
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseMessage> getActiveJobs() {
        List<SysJob> jobs = sysJobService.getActiveJobs();
        return new ResponseEntity<>(
                new ResponseMessage("success", "Liste des jobs actifs", jobs),
                HttpStatus.OK
        );
    }

    @GetMapping("/schedule-type/{scheduleType}")
    public ResponseEntity<ResponseMessage> getJobsByScheduleType(@PathVariable String scheduleType) {
        List<SysJob> jobs = sysJobService.getJobsByScheduleType(scheduleType);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Jobs avec schedule type " + scheduleType, jobs),
                HttpStatus.OK
        );
    }

    @GetMapping("/scope/{scope}")
    public ResponseEntity<ResponseMessage> getJobsByScope(@PathVariable String scope) {
        List<SysJob> jobs = sysJobService.getJobsByScope(scope);
        return new ResponseEntity<>(
                new ResponseMessage("success", "Jobs avec scope " + scope, jobs),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseMessage> toggleJobStatus(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {
        sysJobService.toggleJobStatus(id, enabled);
        String message = enabled ? "Job activé" : "Job désactivé";
        return new ResponseEntity<>(
                new ResponseMessage("success", message, null),
                HttpStatus.OK
        );
    }
}