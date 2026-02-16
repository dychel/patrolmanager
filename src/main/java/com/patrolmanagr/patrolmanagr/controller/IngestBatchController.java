package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.Ingest_batchDTO;
import com.patrolmanagr.patrolmanagr.entity.Ingest_batch;
import com.patrolmanagr.patrolmanagr.config.BatchType;
import com.patrolmanagr.patrolmanagr.config.StatusIngestBatch;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.IngestBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/ingestbatch")
public class IngestBatchController {

    @Autowired
    private IngestBatchService ingestBatchService;

    @PostMapping("/add")
    public ResponseEntity<?> createIngestBatch(@RequestBody Ingest_batchDTO ingestBatchDTO) {
        Ingest_batch savedBatch = ingestBatchService.saveIngestBatch(ingestBatchDTO);
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Batch " + savedBatch.getId() + " créé avec succès", savedBatch),
                HttpStatus.OK
        );
    }

    @PostMapping("/start")
    public ResponseEntity<?> startNewBatch(@RequestParam BatchType batchType,
                                           @RequestParam(required = false) Long vendorId) {
        Ingest_batch newBatch = ingestBatchService.startNewBatch(batchType, vendorId);
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Nouveau batch démarré avec l'ID: " + newBatch.getId(), newBatch),
                HttpStatus.OK
        );
    }

    @PutMapping("/complete/{id}")
    public ResponseEntity<?> completeBatch(@PathVariable Long id,
                                           @RequestParam StatusIngestBatch status,
                                           @RequestParam(required = false) Integer totalRows,
                                           @RequestParam(required = false) Integer acceptedRows,
                                           @RequestParam(required = false) Integer rejectedRows,
                                           @RequestParam(required = false) Integer duplicateRows,
                                           @RequestParam(required = false) String errorMessage) {
        Ingest_batch completedBatch = ingestBatchService.completeBatch(
                id, status, totalRows, acceptedRows, rejectedRows, duplicateRows, errorMessage
        );
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Batch terminé avec succès", completedBatch),
                HttpStatus.OK
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateIngestBatch(@PathVariable Long id,
                                               @RequestBody Ingest_batchDTO ingestBatchDTO) {
        Ingest_batch updatedBatch = ingestBatchService.updateIngestBatch(id, ingestBatchDTO);
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Batch mis à jour avec succès", updatedBatch),
                HttpStatus.OK
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllIngestBatches() {
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Liste des ingest batches", ingestBatchService.listAllIngestBatches()),
                HttpStatus.OK
        );
    }

    @GetMapping("/findbyid/{id}")
    public ResponseEntity<?> findIngestBatchById(@PathVariable Long id) {
        Ingest_batch ingestBatch = ingestBatchService.findIngestBatchById(id);
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Batch trouvé", ingestBatch),
                HttpStatus.OK
        );
    }

    @GetMapping("/findbyvendor/{vendorId}")
    public ResponseEntity<?> findBatchesByVendor(@PathVariable Long vendorId) {
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Batches du vendor ID: " + vendorId,
                        ingestBatchService.findBatchesByVendor(vendorId)),
                HttpStatus.OK
        );
    }

    @GetMapping("/findbystatus/{status}")
    public ResponseEntity<?> findBatchesByStatus(@PathVariable StatusIngestBatch status) {
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Batches avec statut: " + status,
                        ingestBatchService.findBatchesByStatus(status)),
                HttpStatus.OK
        );
    }

    @GetMapping("/findbydaterange")
    public ResponseEntity<?> findBatchesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Batches entre " + startDate + " et " + endDate,
                        ingestBatchService.findBatchesByDateRange(startDate, endDate)),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteIngestBatch(@PathVariable Long id) {
        ingestBatchService.deleteIngestBatchById(id);
        return new ResponseEntity<>(
                new ResponseMessage("delete", "Batch supprimé avec succès"),
                HttpStatus.OK
        );
    }
}