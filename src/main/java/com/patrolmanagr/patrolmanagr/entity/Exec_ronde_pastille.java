package com.patrolmanagr.patrolmanagr.entity;

import com.patrolmanagr.patrolmanagr.config.Status_ronde_pastille;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exec_ronde_pastille")
public class Exec_ronde_pastille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exec_ronde_id", nullable = false)
    private Exec_ronde execRonde;

    @ManyToOne
    @JoinColumn(name = "pastille_id", nullable = false)
    private Ref_pastille pastille;

    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @Column(name = "expected_travel_sec")
    private Integer expectedTravelSec;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status_ronde_pastille status = Status_ronde_pastille.EXPECTED;

    @Column(name = "pointage_id")
    private Long pointageId;

    @Column(name = "actual_travel_sec")
    private Integer actualTravelSec;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "expected_time")
    private LocalDateTime expectedTime;

    @Column(name = "actual_time")
    private LocalDateTime actualTime;

    @Column(name = "deviation_sec")
    private Integer deviationSec;

    @Column(name = "notes")
    private String notes;

    @Column(name = "incident_type")
    private String incidentType;

    @Column(name = "incident_details")
    private String incidentDetails;

    @Column(name = "is_late")
    private Boolean isLate = false;

    @Column(name = "late_minutes")
    private Integer lateMinutes = 0;

    private String auditField;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "created_by")
    private Long created_by;

    @Column(name = "updated_by")
    private Long updated_by;
}