package com.patrolmanagr.patrolmanagr.dto;
import com.patrolmanagr.patrolmanagr.config.Scheduled_Type;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgRondeDTO {

    private Long id;
    @NotNull(message = "L'ID de la ronde est obligatoire")
    private Long refRondeId;
    @NotNull(message = "L'ID du site est obligatoire")
    private Long refSiteId;
    @NotNull(message = "Le type de programmation est obligatoire")
    private Scheduled_Type scheduledType;
    private Long assignedAgentId;
    private Long assignedRondierTerminalId;
    private Long status;
    private String auditField;
    private String schedulePayload;
}