package com.patrolmanagr.patrolmanagr.entity;

import com.patrolmanagr.patrolmanagr.config.ScheduleTypeJob;
import com.patrolmanagr.patrolmanagr.config.JobScope;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sys_job")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SysJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_code", unique = true, nullable = false)
    private String jobCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type_job", nullable = false)
    private ScheduleTypeJob scheduleTypeJob;

    @Column(name = "schedule_time")
    private LocalTime scheduleTime;

    @Column(name = "schedule_interval_min")
    private Integer scheduleIntervalMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_scope")
    private JobScope jobScope;

    @Column(name = "params_json", columnDefinition = "TEXT")
    private String paramsJson;

    @Column(name = "ronde_ids", columnDefinition = "TEXT")
    private String rondeIds;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "audit_field")
    private String auditField;

    private String executionDate;
}