package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ingest_batchDTO;
import com.patrolmanagr.patrolmanagr.entity.Ingest_batch;
import com.patrolmanagr.patrolmanagr.config.BatchType;
import com.patrolmanagr.patrolmanagr.config.StatusIngestBatch;
import java.time.LocalDateTime;
import java.util.List;

public interface IngestBatchService {

    Ingest_batch saveIngestBatch(Ingest_batchDTO ingestBatchDTO);

    Ingest_batch updateIngestBatch(Long id, Ingest_batchDTO ingestBatchDTO);

    Ingest_batch findIngestBatchById(Long id);

    List<Ingest_batch> listAllIngestBatches();

    void deleteIngestBatchById(Long id);

    // Méthodes supplémentaires pour la gestion des batches
    Ingest_batch startNewBatch(BatchType batchType, Long vendorId);

    Ingest_batch completeBatch(Long id, StatusIngestBatch status, Integer totalRows,
                               Integer acceptedRows, Integer rejectedRows,
                               Integer duplicateRows, String errorMessage);

    List<Ingest_batch> findBatchesByVendor(Long vendorId);

    List<Ingest_batch> findBatchesByStatus(StatusIngestBatch status);

    List<Ingest_batch> findBatchesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}