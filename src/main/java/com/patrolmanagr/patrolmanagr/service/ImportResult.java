package com.patrolmanagr.patrolmanagr.service;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private Long batchId;
    private boolean success;
    private String message;
    private int totalRows;
    private int acceptedRows;
    private int rejectedRows;
    private int duplicateRows;
    private List<String> erreurs = new ArrayList<>();

    public ImportResult(Long batchId) {
        this.batchId = batchId;
        this.totalRows = 0;
        this.acceptedRows = 0;
        this.rejectedRows = 0;
        this.duplicateRows = 0;
        this.success = false;
    }

    public void addErreur(String erreur) {
        this.erreurs.add(erreur);
    }

    public void incrementAccepted() {
        this.acceptedRows++;
    }

    public void incrementRejected() {
        this.rejectedRows++;
    }

    public void incrementDuplicate() {
        this.duplicateRows++;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public void setAcceptedRows(int acceptedRows) {
        this.acceptedRows = acceptedRows;
    }

    public void setRejectedRows(int rejectedRows) {
        this.rejectedRows = rejectedRows;
    }

    public void setDuplicateRows(int duplicateRows) {
        this.duplicateRows = duplicateRows;
    }
}