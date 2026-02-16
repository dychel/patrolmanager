package com.patrolmanagr.patrolmanagr.dto;
import com.patrolmanagr.patrolmanagr.config.BatchType;
import com.patrolmanagr.patrolmanagr.config.StatusIngestBatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingest_batchDTO {

    private Long id;
    private BatchType batch_type;
    private Long vendor_id;
    private LocalDateTime started_at;
    private LocalDateTime ended_at;
    private StatusIngestBatch status;
    private Integer total_rows;
    private Integer accepted_rows;
    private Integer rejected_rows;
    private Integer duplicate_rows;
    private String error_message;
    private String audit_field;
}