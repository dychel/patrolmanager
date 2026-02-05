package com.patrolmanagr.patrolmanagr.entity;

import com.patrolmanagr.patrolmanagr.config.JobRunStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sys_job_run")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SysJobRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SysJob job;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobRunStatus status;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "duration_ms")
    private Long durationMs;

    // Champs spécifiques pour le suivi des rondes
    @Column(name = "ronde_count")
    private Integer rondeCount = 0;

    @Column(name = "pointage_count")
    private Integer pointageCount = 0;

    @Column(name = "incident_count")
    private Integer incidentCount = 0;

    @Column(name = "audit_field")
    private String auditField;
}