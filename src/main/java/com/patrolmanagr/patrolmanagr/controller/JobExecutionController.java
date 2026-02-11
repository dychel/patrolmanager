package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.service.JobExecutionService;
import com.patrolmanagr.patrolmanagr.entity.SysJobRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patrolmanagr/job-execution")
public class JobExecutionController {

    @Autowired
    private JobExecutionService jobExecutionService;

    @PostMapping("/execute-manual/{jobId}")
    public ResponseEntity<SysJobRun> executeJobManually(@PathVariable Long jobId) {
        SysJobRun jobRun = jobExecutionService.executeJobManually(jobId);
        return ResponseEntity.ok(jobRun);
    }

    @PostMapping("/force-execute/{jobId}")
    public ResponseEntity<SysJobRun> forceExecuteJob(@PathVariable Long jobId) {
        SysJobRun jobRun = jobExecutionService.forceExecuteJob(jobId);
        return ResponseEntity.ok(jobRun);
    }
}