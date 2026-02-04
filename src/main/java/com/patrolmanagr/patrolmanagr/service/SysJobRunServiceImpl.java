package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.JobRunStatus;
import com.patrolmanagr.patrolmanagr.dto.SysJobRunDTO;
import com.patrolmanagr.patrolmanagr.entity.SysJob;
import com.patrolmanagr.patrolmanagr.entity.SysJobRun;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.SysJobRunRepository;
import com.patrolmanagr.patrolmanagr.repository.SysJobRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SysJobRunServiceImpl implements SysJobRunService {

    @Autowired
    private SysJobRunRepository sysJobRunRepository;

    @Autowired
    private SysJobRepository sysJobRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public SysJobRun createJobRun(SysJobRunDTO jobRunDTO) {
        // Vérifier si le job existe
        SysJob job = sysJobRepository.findById(jobRunDTO.getJobId())
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        SysJobRun jobRun = modelMapper.map(jobRunDTO, SysJobRun.class);
        jobRun.setJob(job);

        // Calculer la durée si endedAt est présent
        if (jobRun.getStartedAt() != null && jobRun.getEndedAt() != null) {
            Duration duration = Duration.between(jobRun.getStartedAt(), jobRun.getEndedAt());
            jobRun.setDurationMs(duration.toMillis());
        }

        return sysJobRunRepository.save(jobRun);
    }

    @Override
    public SysJobRun startJobRun(Long jobId) {
        // Vérifier si le job existe et est actif
        SysJob job = sysJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        if (!job.getIsEnabled()) {
            throw new ApiRequestException("Le job n'est pas actif");
        }

        // Créer un nouveau job run
        SysJobRun jobRun = new SysJobRun();
        jobRun.setJob(job);
        jobRun.setStartedAt(LocalDateTime.now());
        jobRun.setStatus(JobRunStatus.RUNNING);
        jobRun.setMessage("Démarrage de l'exécution");

        return sysJobRunRepository.save(jobRun);
    }

    @Override
    public SysJobRun updateJobRun(Long runId, JobRunStatus status, String message) {
        SysJobRun jobRun = sysJobRunRepository.findById(runId)
                .orElseThrow(() -> new ApiRequestException("Job run non trouvé"));

        jobRun.setStatus(status);
        jobRun.setMessage(message);

        return sysJobRunRepository.save(jobRun);
    }

    @Override
    public SysJobRun completeJobRun(Long runId, JobRunStatus status, String message) {
        SysJobRun jobRun = sysJobRunRepository.findById(runId)
                .orElseThrow(() -> new ApiRequestException("Job run non trouvé"));

        jobRun.setEndedAt(LocalDateTime.now());
        jobRun.setStatus(status);
        jobRun.setMessage(message);

        // Calculer la durée
        if (jobRun.getStartedAt() != null) {
            Duration duration = Duration.between(jobRun.getStartedAt(), jobRun.getEndedAt());
            jobRun.setDurationMs(duration.toMillis());
        }

        return sysJobRunRepository.save(jobRun);
    }

    @Override
    public SysJobRun getJobRunById(Long id) {
        return sysJobRunRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Job run non trouvé"));
    }

    @Override
    public List<SysJobRun> getJobRunsByJobId(Long jobId) {
        return sysJobRunRepository.findByJobId(jobId);
    }

    @Override
    public List<SysJobRun> getJobRunsByStatus(String status) {
        try {
            JobRunStatus jobRunStatus = JobRunStatus.valueOf(status.toUpperCase());
            return sysJobRunRepository.findByStatus(jobRunStatus);
        } catch (IllegalArgumentException e) {
            throw new ApiRequestException("Status invalide: " + status);
        }
    }

    @Override
    public List<SysJobRun> getJobRunsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return sysJobRunRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<SysJobRun> getRunningJobs() {
        return sysJobRunRepository.findRunningJobs();
    }

    @Override
    public void cleanOldRuns(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<SysJobRun> oldRuns = sysJobRunRepository.findByDateRange(
                LocalDateTime.MIN, cutoffDate);

        sysJobRunRepository.deleteAll(oldRuns);
    }

    @Override
    public SysJobRun getLatestJobRun(Long jobId) {
        return sysJobRunRepository.findTopByJobIdOrderByStartedAtDesc(jobId)
                .orElseThrow(() -> new ApiRequestException("Aucun job run trouvé pour ce job"));
    }
}