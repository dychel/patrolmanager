package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.config.Source_Type;
import com.patrolmanagr.patrolmanagr.config.Terminal_Type;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactPointageDTO {

    private LocalDateTime eventTime;
    private Long rondeId;
    private Long siteId;
    private Long pastilleId;
    private String pastilleCodeRaw;
    private Long terminalId;
    private String terminalCodeRaw;
    private Long agentUserId;
    private String agentCodeRaw;
    private String rondeName;
    // Champs d'enrichissement optionnels
    private String siteName;
    private Long zoneId;
    private String zoneName;
    private Long secteurId;
    private String secteurName;
    private String pastilleLabel;
    private Terminal_Type terminalType;
    private Long vendorId;
    private Source_Type sourceType;
    private LocalDate eventDate;
    private Long sourceBatchId;
    private String processedStatus = "PENDING"; // PENDING, PROCESSED, REJECTED, ERROR
    private String rejectionReason;

    // Raison du rejet
    private String validationNotes;

    // Pour traitement batch
    private Boolean skipValidation = false;
    private String importReference;
}