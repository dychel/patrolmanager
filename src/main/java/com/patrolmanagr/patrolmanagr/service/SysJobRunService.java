package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.JobRunStatus;
import com.patrolmanagr.patrolmanagr.dto.SysJobRunDTO;
import com.patrolmanagr.patrolmanagr.entity.SysJobRun;

import java.time.LocalDateTime;
import java.util.List;

public interface SysJobRunService {
    SysJobRun createJobRun(SysJobRunDTO jobRunDTO);
    SysJobRun startJobRun(Long jobId);
    SysJobRun updateJobRun(Long runId, JobRunStatus status, String message);
    SysJobRun completeJobRun(Long runId, JobRunStatus status, String message);
    SysJobRun getJobRunById(Long id);
    List<SysJobRun> getJobRunsByJobId(Long jobId);
    List<SysJobRun> getJobRunsByStatus(String status);
    List<SysJobRun> getJobRunsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<SysJobRun> getRunningJobs();
    void cleanOldRuns(int daysToKeep);
    SysJobRun getLatestJobRun(Long jobId);
}