package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.Status;
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

    @NotNull(message = "L'ID de la programmation est obligatoire")
    private Long progRondeId;

    @NotNull(message = "L'ID de la ronde est obligatoire")
    private Long refRondeId;

    @NotNull(message = "L'ID du site est obligatoire")
    private Long siteId;

    private Status_exec_Ronde status = Status_exec_Ronde.PLANNED;

    private BigDecimal completionRate;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private LocalDateTime lastEventAt;

    private String auditField;
}