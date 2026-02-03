package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.FactPointageDTO;
import com.patrolmanagr.patrolmanagr.entity.Fact_pointage;

import java.time.LocalDate;
import java.util.List;

public interface FactPointageService {

    Fact_pointage savePointage(FactPointageDTO factPointageDTO);
    List<Fact_pointage> savePointageBatch(List<FactPointageDTO> pointagesDTO);
    Fact_pointage updatePointage(Long id, FactPointageDTO factPointageDTO);
    Fact_pointage findPointageById(Long id);
    List<Fact_pointage> findAllPointages();
    void deletePointageById(Long id);

    // Recherches spécifiques
    List<Fact_pointage> findBySiteAndPeriod(Long siteId, LocalDate startDate, LocalDate endDate);
    List<Fact_pointage> findByRondeAndPeriod(Long rondeId, LocalDate startDate, LocalDate endDate);
    List<Fact_pointage> findByAgent(Long agentUserId);
    List<Fact_pointage> findRejectedPointages();
    List<Fact_pointage> findPendingPointages();

    // Validation
    Fact_pointage validatePointage(Long id, String validationNotes);
    Fact_pointage rejectPointage(Long id, String rejectionReason);

    // Recherche par external_uid (pastille_code_raw)
    List<Fact_pointage> findByExternalUid(String externalUid);
}