package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Ingest_batch;
import com.patrolmanagr.patrolmanagr.config.BatchType;
import com.patrolmanagr.patrolmanagr.config.StatusIngestBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IngestBatchRepository extends JpaRepository<Ingest_batch, Long> {

    @Query("SELECT ib FROM Ingest_batch ib WHERE ib.id = :id")
    Ingest_batch findById_ingest_batch(@Param("id") Long id);

    @Query("SELECT ib FROM Ingest_batch ib WHERE ib.batch_type = :batchType")
    List<Ingest_batch> findByBatchType(@Param("batchType") BatchType batchType);

    @Query("SELECT ib FROM Ingest_batch ib WHERE ib.vendor_id = :vendorId")
    List<Ingest_batch> findByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT ib FROM Ingest_batch ib WHERE ib.status = :status")
    List<Ingest_batch> findByStatus(@Param("status") StatusIngestBatch status);

    @Query("SELECT ib FROM Ingest_batch ib WHERE ib.started_at BETWEEN :startDate AND :endDate")
    List<Ingest_batch> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ib FROM Ingest_batch ib WHERE ib.is_deleted = false")
    List<Ingest_batch> findAllActive();
}