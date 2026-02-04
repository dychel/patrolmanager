package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.SysJobDTO;
import com.patrolmanagr.patrolmanagr.entity.SysJob;

import java.util.List;

public interface SysJobService {

    SysJob createJob(SysJobDTO jobDTO);

    SysJob updateJob(Long id, SysJobDTO jobDTO);

    void deleteJob(Long id);

    SysJob getJobById(Long id);

    SysJob getJobByCode(String jobCode);

    List<SysJob> getAllJobs();

    List<SysJob> getActiveJobs();

    List<SysJob> getJobsByScheduleType(String scheduleType);

    List<SysJob> getJobsByScope(String scope);

    void toggleJobStatus(Long id, Boolean isEnabled);
}