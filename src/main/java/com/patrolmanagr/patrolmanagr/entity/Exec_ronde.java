package com.patrolmanagr.patrolmanagr.entity;
import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exec_ronde")
public class Exec_ronde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exec_date", nullable = false)
    private LocalDate execDate;

    @Column(name = "planned_start_at", nullable = false)
    private LocalDateTime plannedStartAt;

    @Column(name = "planned_end_at", nullable = false)
    private LocalDateTime plannedEndAt;

    @ManyToOne
    @JoinColumn(name = "ref_ronde_id", nullable = false)
    private Ref_ronde refRonde;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Ref_site site;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status_exec_Ronde status = Status_exec_Ronde.PLANNED;

    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "last_event_at")
    private LocalDateTime lastEventAt;

    // Champs pour la gestion des incidents
    @Column(name = "incident_count")
    private Integer incidentCount = 0;

    @Column(name = "retard_minutes")
    private Integer retardMinutes = 0;

    @Column(name = "pastilles_manquantes")
    private Integer pastillesManquantes = 0;

    @Column(name = "sequence_errors")
    private Integer sequenceErrors = 0;

    // Lien avec le job qui a créé cette exécution
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_run_id")
    private SysJobRun jobRun;

    private String auditField;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "created_by")
    private Long created_by;

    @Column(name = "updated_by")
    private Long updated_by;

    // Méthode utilitaire
    public void addIncident() {
        this.incidentCount = (this.incidentCount == null) ? 1 : this.incidentCount + 1;
    }
}