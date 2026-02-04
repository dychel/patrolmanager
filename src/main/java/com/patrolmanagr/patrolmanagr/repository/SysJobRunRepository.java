package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.SysJobRun;
import com.patrolmanagr.patrolmanagr.config.JobRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SysJobRunRepository extends JpaRepository<SysJobRun, Long> {

    List<SysJobRun> findByJobId(Long jobId);

    List<SysJobRun> findByStatus(JobRunStatus status);

    List<SysJobRun> findByJobIdAndStatus(Long jobId, JobRunStatus status);

    @Query("SELECT r FROM SysJobRun r WHERE r.job.id = :jobId ORDER BY r.startedAt DESC")
    List<SysJobRun> findLatestByJobId(@Param("jobId") Long jobId);

    @Query("SELECT r FROM SysJobRun r WHERE r.startedAt >= :startDate AND r.startedAt <= :endDate")
    List<SysJobRun> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM SysJobRun r WHERE r.job.id = :jobId AND r.startedAt >= :startDate AND r.startedAt <= :endDate")
    List<SysJobRun> findByJobIdAndDateRange(@Param("jobId") Long jobId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM SysJobRun r WHERE r.status = 'RUNNING'")
    List<SysJobRun> findRunningJobs();

    @Query("SELECT COUNT(r) FROM SysJobRun r WHERE r.job.id = :jobId AND r.status = 'SUCCESS'")
    Long countSuccessfulRuns(@Param("jobId") Long jobId);

    @Query("SELECT COUNT(r) FROM SysJobRun r WHERE r.job.id = :jobId")
    Long countTotalRuns(@Param("jobId") Long jobId);

    Optional<SysJobRun> findTopByJobIdOrderByStartedAtDesc(Long jobId);
}