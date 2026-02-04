package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.JobRunStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysJobRunDTO {
    private Long id;
    private Long jobId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private JobRunStatus status;
    private String message;
    private Long durationMs;
    private Integer rondeCount;
    private Integer pointageCount;
    private Integer incidentCount;
    private String auditField;
}