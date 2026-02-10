package com.patrolmanagr.patrolmanagr.entity;

import com.patrolmanagr.patrolmanagr.config.IncidentSeverity;
import com.patrolmanagr.patrolmanagr.config.IncidentType;
import com.patrolmanagr.patrolmanagr.config.IncidentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "incident")
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IncidentSeverity severity = IncidentSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exec_ronde_id", nullable = false)
    private Exec_ronde execRonde;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exec_ronde_pastille_id")
    private Exec_ronde_pastille execRondePastille;

    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(name = "ronde_id", nullable = false)
    private Long rondeId;

    @Column(name = "pastille_id")
    private Long pastilleId;

    @Column(name = "pointage_id")
    private Long pointageId;

    @Column(name = "agent_user_id")
    private Long agentUserId;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    // Métriques de retard
    @Column(name = "delay_minutes")
    private Integer delayMinutes = 0;

    @Column(name = "expected_time")
    private LocalDateTime expectedTime;

    @Column(name = "actual_time")
    private LocalDateTime actualTime;

    @Column(name = "audit_field")
    private String auditField;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}