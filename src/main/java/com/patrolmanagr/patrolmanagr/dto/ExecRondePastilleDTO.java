package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.Status_ronde_pastille;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExecRondePastilleDTO {
    private Long id;

    @NotNull(message = "L'ID de l'exécution de ronde est obligatoire")
    private Long execRondeId;

    @NotNull(message = "L'ID de la pastille est obligatoire")
    private Long pastilleId;

    @NotNull(message = "Le numéro de séquence est obligatoire")
    private Integer seqNo;

    private Integer expectedTravelSec;

    private Status_ronde_pastille status = Status_ronde_pastille.EXPECTED;

    private Long pointageId;

    private Integer actualTravelSec;

    private LocalDateTime scannedAt;

    private LocalDateTime expectedTime;

    private LocalDateTime actualTime;

    private Integer deviationSec;

    private String notes;

    private String auditField;
}