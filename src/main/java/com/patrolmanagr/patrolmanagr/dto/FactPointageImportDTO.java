package com.patrolmanagr.patrolmanagr.dto;
import com.patrolmanagr.patrolmanagr.config.Source_Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactPointageImportDTO {

    // Données brutes de l'API
    private LocalDateTime eventTime;
    private String externalUid;      // RFID/NFC code
    private String terminalCode;     // Code du terminal
    private String agentCode;        // Code de l'agent (badge)
    private Long siteId;             // ID du site
    private Long rondeId;            // ID de la ronde (optionnel)

    // Métadonnées de l'import
    private Source_Type sourceType;
    private Long batchId;
    private Long vendorId;         // ID du fournisseur
    private String rawData;          // Données brutes JSON/XML

    // Pour le matching
    private Boolean forceProcessing = false;
}