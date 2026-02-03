package com.patrolmanagr.patrolmanagr.entity;

import com.patrolmanagr.patrolmanagr.config.Source_Type;
import com.patrolmanagr.patrolmanagr.config.Terminal_Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fact_pointage")
public class Fact_pointage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime; // Horodate pointage

    @Column(name = "ronde_id")
    private Long rondeId;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate; // Dérivé pour index

    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(name = "pastille_id")
    private Long pastilleId; // Null si pastille inconnue = rejet

    @Column(name = "pastille_code_raw", length = 120)
    private String pastilleCodeRaw;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(name = "terminal_code_raw", length = 120)
    private String terminalCodeRaw;

    @Column(name = "agent_user_id")
    private Long agentUserId; // Agent connu

    @Column(name = "agent_code_raw", length = 120)
    private String agentCodeRaw;

    @Column(name = "agent_name", length = 120)
    private String agentName;

    // Champs d'enrichissement (dénormalisation)
    @Column(name = "site_name", nullable = false, length = 255)
    private String siteName;

    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "zone_name", length = 255)
    private String zoneName;

    @Column(name = "secteur_id")
    private Long secteurId;

    @Column(name = "secteur_name", length = 255)
    private String secteurName;

    @Column(name = "ronde_name", length = 255)
    private String rondeName;

    @Column(name = "pastille_label", length = 180)
    private String pastilleLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "terminal_type", length = 20)
    private Terminal_Type terminalType;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private Source_Type sourceType;

    @Column(name = "source_batch_id")
    private Long sourceBatchId; // Pour import/IoT API

    // Champs techniques/historiques
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    @Column(name = "processed_status", length = 20)
    private String processedStatus = "PENDING"; // PENDING, PROCESSED, REJECTED, ERROR

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "validation_notes", length = 500)
    private String validationNotes;

    // Index pour optimiser les requêtes
    @PrePersist
    @PreUpdate
    private void setEventDate() {
        if (eventTime != null && eventDate == null) {
            this.eventDate = eventTime.toLocalDate();
        }
    }
}