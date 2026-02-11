package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.EvenementSeverity;
import com.patrolmanagr.patrolmanagr.config.EvenementType;
import com.patrolmanagr.patrolmanagr.config.EvenementStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvenementDTO {
    private Long id;
    private EvenementType type;
    private EvenementSeverity severity;
    private EvenementStatus status;
    private String title;
    private String description;

    // Références
    private Long execRondeId;
    private Long execRondePastilleId;
    private Long jobRunId;
    private Long siteId;
    private Long rondeId;
    private Long pastilleId;
    private Long pointageId;
    private Long agentUserId;

    // Métriques
    private Integer delayMinutes;
    private LocalDateTime expectedTime;
    private LocalDateTime actualTime;
    private Integer deviationSec;
    private Integer expectedTravelSec;
    private Integer actualTravelSec;

    private String auditField;
    private LocalDateTime detectedAt;
}