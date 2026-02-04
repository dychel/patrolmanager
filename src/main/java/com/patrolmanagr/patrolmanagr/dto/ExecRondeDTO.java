package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExecRondeDTO {
    private Long id;

    @NotNull(message = "La date d'exécution est obligatoire")
    private LocalDate execDate;

    @NotNull(message = "La date de début prévue est obligatoire")
    private LocalDateTime plannedStartAt;

    @NotNull(message = "La date de fin prévue est obligatoire")
    private LocalDateTime plannedEndAt;

    @NotNull(message = "L'ID de la ronde est obligatoire")
    private Long refRondeId;

    private String rondeCode;
    private String rondeName;

    @NotNull(message = "L'ID du site est obligatoire")
    private Long siteId;

    private String siteName;

    private Status_exec_Ronde status = Status_exec_Ronde.PLANNED;

    private BigDecimal completionRate;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime lastEventAt;

    // Nouveaux champs pour les incidents
    private Integer incidentCount = 0;
    private Integer retardMinutes = 0;
    private Integer pastillesManquantes = 0;
    private Integer sequenceErrors = 0;
    private Long jobRunId;
    private String auditField;
}