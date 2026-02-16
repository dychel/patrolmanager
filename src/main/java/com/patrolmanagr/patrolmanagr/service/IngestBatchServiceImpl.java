package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ingest_batchDTO;
import com.patrolmanagr.patrolmanagr.entity.Ingest_batch;
import com.patrolmanagr.patrolmanagr.config.BatchType;
import com.patrolmanagr.patrolmanagr.config.StatusIngestBatch;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.IngestBatchRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IngestBatchServiceImpl implements IngestBatchService {

    @Autowired
    private IngestBatchRepository ingestBatchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public Ingest_batch saveIngestBatch(Ingest_batchDTO ingestBatchDTO) {
        Ingest_batch ingestBatch = modelMapper.map(ingestBatchDTO, Ingest_batch.class);
        ingestBatch.setCreated_by(userService.getConnectedUserId());
        ingestBatch.setStarted_at(LocalDateTime.now());
        ingestBatch.setStatus(StatusIngestBatch.RUNNING);
        return ingestBatchRepository.save(ingestBatch);
    }

    @Override
    @Transactional
    public Ingest_batch updateIngestBatch(Long id, Ingest_batchDTO ingestBatchDTO) {
        Ingest_batch ingestBatchToUpdate = ingestBatchRepository.findById_ingest_batch(id);
        if (ingestBatchToUpdate == null) {
            throw new ApiRequestException("Ingest batch non trouvé avec l'ID: " + id);
        }

        Ingest_batch ingestBatch = modelMapper.map(ingestBatchDTO, Ingest_batch.class);
        ingestBatch.setId(ingestBatchToUpdate.getId());
        ingestBatch.setUpdated_at(LocalDateTime.now());
        ingestBatch.setUpdated_by(userService.getConnectedUserId());

        // Conserver les dates de création
        ingestBatch.setCreated_at(ingestBatchToUpdate.getCreated_at());
        ingestBatch.setCreated_by(ingestBatchToUpdate.getCreated_by());

        return ingestBatchRepository.save(ingestBatch);
    }

    @Override
    public Ingest_batch findIngestBatchById(Long id) {
        Ingest_batch ingestBatch = ingestBatchRepository.findById_ingest_batch(id);
        if (ingestBatch == null) {
            throw new ApiRequestException("Ingest batch non trouvé avec l'ID: " + id);
        }
        return ingestBatch;
    }

    @Override
    public List<Ingest_batch> listAllIngestBatches() {
        List<Ingest_batch> list = ingestBatchRepository.findAllActive();
        if (list.isEmpty()) {
            throw new ApiRequestException("Aucun ingest batch enregistré dans la base de données");
        }
        return list;
    }

    @Override
    @Transactional
    public void deleteIngestBatchById(Long id) {
        Ingest_batch ingestBatch = ingestBatchRepository.findById_ingest_batch(id);
        if (ingestBatch == null) {
            throw new ApiRequestException("Ingest batch non trouvé avec l'ID: " + id);
        }

        // Soft delete
        ingestBatch.setIs_deleted(true);
        ingestBatch.setDeleted_at(LocalDateTime.now());
        ingestBatch.setDeleted_by(userService.getConnectedUserId());
        ingestBatchRepository.save(ingestBatch);
    }

    @Override
    @Transactional
    public Ingest_batch startNewBatch(BatchType batchType, Long vendorId) {
        Ingest_batch newBatch = new Ingest_batch();
        newBatch.setBatch_type(batchType);
        newBatch.setVendor_id(vendorId);
        newBatch.setStarted_at(LocalDateTime.now());
        newBatch.setStatus(StatusIngestBatch.RUNNING);
        newBatch.setCreated_by(userService.getConnectedUserId());

        return ingestBatchRepository.save(newBatch);
    }

    @Override
    @Transactional
    public Ingest_batch completeBatch(Long id, StatusIngestBatch status, Integer totalRows,
                                      Integer acceptedRows, Integer rejectedRows,
                                      Integer duplicateRows, String errorMessage) {
        Ingest_batch ingestBatch = findIngestBatchById(id);

        ingestBatch.setEnded_at(LocalDateTime.now());
        ingestBatch.setStatus(status);
        ingestBatch.setTotal_rows(totalRows);
        ingestBatch.setAccepted_rows(acceptedRows);
        ingestBatch.setRejected_rows(rejectedRows);
        ingestBatch.setDuplicate_rows(duplicateRows);
        ingestBatch.setError_message(errorMessage);
        ingestBatch.setUpdated_at(LocalDateTime.now());
        ingestBatch.setUpdated_by(userService.getConnectedUserId());

        return ingestBatchRepository.save(ingestBatch);
    }

    @Override
    public List<Ingest_batch> findBatchesByVendor(Long vendorId) {
        List<Ingest_batch> batches = ingestBatchRepository.findByVendorId(vendorId);
        if (batches.isEmpty()) {
            throw new ApiRequestException("Aucun batch trouvé pour le vendor ID: " + vendorId);
        }
        return batches;
    }

    @Override
    public List<Ingest_batch> findBatchesByStatus(StatusIngestBatch status) {
        List<Ingest_batch> batches = ingestBatchRepository.findByStatus(status);
        if (batches.isEmpty()) {
            throw new ApiRequestException("Aucun batch trouvé avec le statut: " + status);
        }
        return batches;
    }

    @Override
    public List<Ingest_batch> findBatchesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Ingest_batch> batches = ingestBatchRepository.findByDateRange(startDate, endDate);
        if (batches.isEmpty()) {
            throw new ApiRequestException("Aucun batch trouvé dans la période spécifiée");
        }
        return batches;
    }
}