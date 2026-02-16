package com.patrolmanagr.patrolmanagr.entity;

import com.patrolmanagr.patrolmanagr.config.BatchType;
import com.patrolmanagr.patrolmanagr.config.StatusIngestBatch;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Ingest_batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BatchType batch_type;

    private Long vendor_id;

    private LocalDateTime started_at = LocalDateTime.now();

    private LocalDateTime ended_at;

    @Enumerated(EnumType.STRING)
    private StatusIngestBatch status;

    private Integer total_rows;

    private Integer accepted_rows;

    private Integer rejected_rows;

    private Integer duplicate_rows;

    @Column(columnDefinition = "TEXT")
    private String error_message;

    @Column(columnDefinition = "TEXT")
    private String audit_field;

    //champs historique activités
    private LocalDateTime created_at = LocalDateTime.now();
    private Long created_by;
    private LocalDateTime updated_at;
    private Long updated_by;
    private Boolean is_deleted = false;
    private LocalDateTime deleted_at;
    private Long deleted_by;
}