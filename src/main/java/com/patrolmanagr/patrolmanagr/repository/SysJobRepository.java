package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.SysJob;
import com.patrolmanagr.patrolmanagr.config.ScheduleTypeJob;
import com.patrolmanagr.patrolmanagr.config.JobScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysJobRepository extends JpaRepository<SysJob, Long> {

    Optional<SysJob> findByJobCode(String jobCode);

    List<SysJob> findByIsEnabledTrue();

    List<SysJob> findByScheduleTypeJob(ScheduleTypeJob scheduleTypeJob);

    List<SysJob> findByJobScope(JobScope jobScope);

    @Query("SELECT j FROM SysJob j WHERE j.jobCode = :jobCode")
    SysJob findByCode(@Param("jobCode") String jobCode);

    @Query("SELECT j FROM SysJob j WHERE j.scheduleTypeJob = :scheduleType AND j.isEnabled = true")
    List<SysJob> findActiveByScheduleType(@Param("scheduleType") ScheduleTypeJob scheduleType);

    @Query("SELECT j FROM SysJob j WHERE j.jobScope = :scope AND j.isEnabled = true")
    List<SysJob> findActiveByScope(@Param("scope") JobScope scope);
}