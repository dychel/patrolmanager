package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.IncidentSeverity;
import com.patrolmanagr.patrolmanagr.config.IncidentStatus;
import com.patrolmanagr.patrolmanagr.config.IncidentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {
    private Long id;
    private IncidentType type;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String title;
    private String description;
    private Long execRondeId;
    private Long execRondePastilleId;
    private Long siteId;
    private Long rondeId;
    private Long pastilleId;
    private Long pointageId;
    private Long agentUserId;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private Integer delayMinutes;
    private LocalDateTime expectedTime;
    private LocalDateTime actualTime;
    private String auditField;
}