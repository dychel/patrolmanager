package com.patrolmanagr.patrolmanagr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactPointageBatchResponse {
    private String batchId;
    private LocalDateTime importDate;
    private int totalRecords;
    private int processedCount;
    private int rejectedCount;
    private List<FactPointageDTO> processedPointages;
    private List<BatchError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchError {
        private int recordIndex;
        private String externalUid;
        private String errorMessage;
        private String rawData;
    }
}