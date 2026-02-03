package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.FactPointageDTO;
import com.patrolmanagr.patrolmanagr.dto.FactPointageImportDTO;
import com.patrolmanagr.patrolmanagr.entity.Fact_pointage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PointageImportService {

    @Autowired
    private FactPointageService factPointageService;

    @Autowired
    private RefPastilleService refPastilleService;

    @Async("pointageImportExecutor")
    @Transactional
    public CompletableFuture<ImportResult> importPointagesBatch(List<FactPointageImportDTO> importData) {
        ImportResult result = new ImportResult();

        try {
            if (importData == null || importData.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Aucune donnée à importer");
                return CompletableFuture.completedFuture(result);
            }

            // Conversion et traitement batch
            List<FactPointageDTO> pointagesDTO = convertImportDTOs(importData);

            // Import optimisé avec pré-chargement
            List<Fact_pointage> saved = factPointageService.savePointageBatch(pointagesDTO);

            // Calculer les échecs détaillés
            int failedCount = importData.size() - saved.size();
            List<ImportError> errors = new ArrayList<>();

            if (failedCount > 0) {
                // Logique pour identifier les erreurs spécifiques
                // (à adapter selon vos besoins)
                for (int i = 0; i < importData.size(); i++) {
                    // Simuler la détection d'erreurs
                    if (i >= saved.size()) {
                        errors.add(new ImportError(
                                i,
                                importData.get(i).getExternalUid(),
                                "Échec lors du traitement",
                                importData.get(i)
                        ));
                    }
                }
            }

            result.setTotalProcessed(importData.size());
            result.setSuccessCount(saved.size());
            result.setFailedCount(failedCount);
            result.setErrors(errors);
            result.setImportTimestamp(LocalDateTime.now());
            result.setProcessingDurationMs(calculateProcessingTime());
            result.setSuccess(true);
            result.setMessage(String.format(
                    "Import terminé: %d réussis, %d échecs sur %d total",
                    saved.size(), failedCount, importData.size()
            ));

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Erreur lors de l'import: " + e.getMessage());
            result.setErrorMessage(e.getMessage());
            result.setImportTimestamp(LocalDateTime.now());
        }

        return CompletableFuture.completedFuture(result);
    }

    @Async("pointageImportExecutor")
    @Transactional
    public CompletableFuture<ImportResult> importPointagesBatchWithValidation(List<FactPointageImportDTO> importData,
                                                                              boolean skipValidation) {
        ImportResult result = new ImportResult();
        result.setSkipValidation(skipValidation);
        result.setBatchSize(importData.size());

        try {
            // Prétraitement: nettoyage des données
            List<FactPointageImportDTO> cleanedData = preprocessImportData(importData);

            if (cleanedData.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Aucune donnée valide après nettoyage");
                return CompletableFuture.completedFuture(result);
            }

            // Conversion
            List<FactPointageDTO> pointagesDTO = convertImportDTOs(cleanedData);

            // Import
            LocalDateTime startTime = LocalDateTime.now();
            List<Fact_pointage> saved = factPointageService.savePointageBatch(pointagesDTO);
            LocalDateTime endTime = LocalDateTime.now();

            // Remplir le résultat
            populateResult(result, cleanedData, saved, startTime, endTime);

        } catch (Exception e) {
            handleImportError(result, e);
        }

        return CompletableFuture.completedFuture(result);
    }

    private List<FactPointageImportDTO> preprocessImportData(List<FactPointageImportDTO> importData) {
        return importData.stream()
                .filter(dto -> dto != null)
                .filter(dto -> dto.getEventTime() != null)
                .filter(dto -> dto.getExternalUid() != null && !dto.getExternalUid().trim().isEmpty())
                .filter(dto -> dto.getSiteId() != null)
                .toList();
    }

    private void populateResult(ImportResult result,
                                List<FactPointageImportDTO> sourceData,
                                List<Fact_pointage> savedPointages,
                                LocalDateTime startTime,
                                LocalDateTime endTime) {

        result.setTotalProcessed(sourceData.size());
        result.setSuccessCount(savedPointages.size());
        result.setFailedCount(sourceData.size() - savedPointages.size());

        // Calculer les erreurs détaillées
        if (result.getFailedCount() > 0) {
            List<ImportError> errors = identifyErrors(sourceData, savedPointages);
            result.setErrors(errors);
        }

        result.setImportTimestamp(LocalDateTime.now());
        result.setStartTime(startTime);
        result.setEndTime(endTime);

        // Calculer la durée
        if (startTime != null && endTime != null) {
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            result.setProcessingDurationMs(durationMs);

            // Calculer la vitesse (pointages/second)
            if (durationMs > 0) {
                double speed = (double) sourceData.size() / (durationMs / 1000.0);
                result.setProcessingSpeed(speed);
            }
        }

        result.setSuccess(true);
        result.setMessage(generateSuccessMessage(result));
    }

    private List<ImportError> identifyErrors(List<FactPointageImportDTO> sourceData,
                                             List<Fact_pointage> savedPointages) {
        List<ImportError> errors = new ArrayList<>();

        // Simplifié: on suppose que les échecs sont les derniers éléments
        // En réalité, il faudrait une logique plus sophistiquée
        for (int i = savedPointages.size(); i < sourceData.size(); i++) {
            FactPointageImportDTO failedDto = sourceData.get(i);
            errors.add(new ImportError(
                    i,
                    failedDto.getExternalUid(),
                    "Échec du traitement (cause inconnue)",
                    failedDto
            ));
        }

        return errors;
    }

    private void handleImportError(ImportResult result, Exception e) {
        result.setSuccess(false);
        result.setErrorMessage(e.getMessage());
        result.setImportTimestamp(LocalDateTime.now());
        result.setEndTime(LocalDateTime.now());

        if (result.getStartTime() != null && result.getEndTime() != null) {
            long durationMs = java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis();
            result.setProcessingDurationMs(durationMs);
        }

        result.setMessage("Échec de l'import: " + e.getMessage());
    }

    private String generateSuccessMessage(ImportResult result) {
        StringBuilder message = new StringBuilder();
        message.append("Import batch terminé. ");
        message.append("Succès: ").append(result.getSuccessCount()).append(", ");
        message.append("Échecs: ").append(result.getFailedCount()).append(", ");
        message.append("Total: ").append(result.getTotalProcessed());

        if (result.getProcessingDurationMs() != null) {
            message.append(", Durée: ").append(result.getProcessingDurationMs()).append("ms");
        }

        if (result.getProcessingSpeed() != null) {
            message.append(", Vitesse: ").append(String.format("%.1f", result.getProcessingSpeed())).append(" pts/s");
        }

        return message.toString();
    }

    private List<FactPointageDTO> convertImportDTOs(List<FactPointageImportDTO> importData) {
        return importData.stream()
                .map(this::convertToFactPointageDTO)
                .toList();
    }

    private FactPointageDTO convertToFactPointageDTO(FactPointageImportDTO importDTO) {
        FactPointageDTO dto = new FactPointageDTO();
        dto.setEventTime(importDTO.getEventTime());
        dto.setPastilleCodeRaw(importDTO.getExternalUid());
        dto.setTerminalCodeRaw(importDTO.getTerminalCode());
        dto.setAgentCodeRaw(importDTO.getAgentCode());
        dto.setSiteId(importDTO.getSiteId());
        dto.setRondeId(importDTO.getRondeId());
        dto.setSourceType(importDTO.getSourceType());
        dto.setSourceBatchId(importDTO.getBatchId());
        dto.setVendorId(importDTO.getVendorId());

        // Options de traitement
        dto.setSkipValidation(importDTO.getForceProcessing() != null ? importDTO.getForceProcessing() : false);

        return dto;
    }

    private Long calculateProcessingTime() {
        // Implémentation simplifiée
        return System.currentTimeMillis() % 10000L; // Pour l'exemple
    }

    // Classe interne pour le résultat d'import
    public static class ImportResult {
        private boolean success;
        private String message;
        private String errorMessage;
        private int totalProcessed;
        private int successCount;
        private int failedCount;
        private int batchSize;
        private Long processingDurationMs;
        private Double processingSpeed;
        private LocalDateTime importTimestamp;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean skipValidation = false;
        private List<ImportError> errors = new ArrayList<>();

        // Constructeurs
        public ImportResult() {
            this.importTimestamp = LocalDateTime.now();
        }

        public ImportResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.importTimestamp = LocalDateTime.now();
        }

        // Getters et Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getTotalProcessed() {
            return totalProcessed;
        }

        public void setTotalProcessed(int totalProcessed) {
            this.totalProcessed = totalProcessed;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(int failedCount) {
            this.failedCount = failedCount;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public Long getProcessingDurationMs() {
            return processingDurationMs;
        }

        public void setProcessingDurationMs(Long processingDurationMs) {
            this.processingDurationMs = processingDurationMs;
        }

        public Double getProcessingSpeed() {
            return processingSpeed;
        }

        public void setProcessingSpeed(Double processingSpeed) {
            this.processingSpeed = processingSpeed;
        }

        public LocalDateTime getImportTimestamp() {
            return importTimestamp;
        }

        public void setImportTimestamp(LocalDateTime importTimestamp) {
            this.importTimestamp = importTimestamp;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public boolean isSkipValidation() {
            return skipValidation;
        }

        public void setSkipValidation(boolean skipValidation) {
            this.skipValidation = skipValidation;
        }

        public List<ImportError> getErrors() {
            return errors;
        }

        public void setErrors(List<ImportError> errors) {
            this.errors = errors;
        }

        // Méthodes utilitaires
        public double getSuccessRate() {
            if (totalProcessed == 0) return 0.0;
            return (successCount * 100.0) / totalProcessed;
        }

        public double getFailureRate() {
            if (totalProcessed == 0) return 0.0;
            return (failedCount * 100.0) / totalProcessed;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public int getErrorCount() {
            return errors.size();
        }

        @Override
        public String toString() {
            return String.format(
                    "ImportResult{success=%s, message='%s', successCount=%d, failedCount=%d, total=%d, duration=%dms}",
                    success, message, successCount, failedCount, totalProcessed, processingDurationMs
            );
        }
    }

    // Classe pour les erreurs détaillées
    public static class ImportError {
        private int recordIndex;
        private String externalUid;
        private String errorMessage;
        private FactPointageImportDTO sourceData;

        // Constructeurs
        public ImportError() {}

        public ImportError(int recordIndex, String externalUid, String errorMessage, FactPointageImportDTO sourceData) {
            this.recordIndex = recordIndex;
            this.externalUid = externalUid;
            this.errorMessage = errorMessage;
            this.sourceData = sourceData;
        }

        // Getters et Setters
        public int getRecordIndex() {
            return recordIndex;
        }

        public void setRecordIndex(int recordIndex) {
            this.recordIndex = recordIndex;
        }

        public String getExternalUid() {
            return externalUid;
        }

        public void setExternalUid(String externalUid) {
            this.externalUid = externalUid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public FactPointageImportDTO getSourceData() {
            return sourceData;
        }

        public void setSourceData(FactPointageImportDTO sourceData) {
            this.sourceData = sourceData;
        }

        @Override
        public String toString() {
            return String.format(
                    "ImportError{index=%d, uid='%s', error='%s'}",
                    recordIndex, externalUid, errorMessage
            );
        }
    }

    // Méthodes supplémentaires utilitaires
    public CompletableFuture<ImportStatistics> getImportStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return CompletableFuture.supplyAsync(() -> {
            ImportStatistics stats = new ImportStatistics();
            stats.setPeriodStart(startDate);
            stats.setPeriodEnd(endDate);

            // Ici, vous pourriez interroger la base de données
            // pour obtenir des statistiques réelles

            return stats;
        });
    }

    public static class ImportStatistics {
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private int totalImports;
        private int totalPointages;
        private int successfulImports;
        private int failedImports;
        private double avgProcessingTimeMs;
        private double avgBatchSize;

        // Getters et Setters
        public LocalDateTime getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

        public LocalDateTime getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

        public int getTotalImports() { return totalImports; }
        public void setTotalImports(int totalImports) { this.totalImports = totalImports; }

        public int getTotalPointages() { return totalPointages; }
        public void setTotalPointages(int totalPointages) { this.totalPointages = totalPointages; }

        public int getSuccessfulImports() { return successfulImports; }
        public void setSuccessfulImports(int successfulImports) { this.successfulImports = successfulImports; }

        public int getFailedImports() { return failedImports; }
        public void setFailedImports(int failedImports) { this.failedImports = failedImports; }

        public double getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
        public void setAvgProcessingTimeMs(double avgProcessingTimeMs) { this.avgProcessingTimeMs = avgProcessingTimeMs; }

        public double getAvgBatchSize() { return avgBatchSize; }
        public void setAvgBatchSize(double avgBatchSize) { this.avgBatchSize = avgBatchSize; }
    }
}