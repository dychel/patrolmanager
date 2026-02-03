package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.entity.Fact_pointage;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.repository.FactPointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patrolmanagr/monitoring")
public class MonitoringController {

    @Autowired
    private FactPointageRepository factPointageRepository;

    @GetMapping("/performance")
    public ResponseEntity<ResponseMessage> getPerformanceStats() {
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusMonths(1);

        long totalPointages = factPointageRepository.count();
        long pendingCount = factPointageRepository.countByProcessedStatus("PENDING");
        long processedCount = factPointageRepository.countByProcessedStatus("PROCESSED");
        long rejectedCount = factPointageRepository.countByProcessedStatus("REJECTED");

        List<Fact_pointage> monthlyPointages = factPointageRepository
                .findByEventDateBetween(monthAgo, today);

        Map<String, Object> stats = Map.of(
                "totalPointages", totalPointages,
                "pending", pendingCount,
                "processed", processedCount,
                "rejected", rejectedCount,
                "monthlyCount", monthlyPointages.size(),
                "avgDaily", monthlyPointages.size() / 30
        );

        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Statistiques de performance",
                stats
        ), HttpStatus.OK);
    }

    @GetMapping("/integrity")
    public ResponseEntity<ResponseMessage> checkDataIntegrity() {
        List<Fact_pointage> orphanPointages = factPointageRepository.findAll()
                .stream()
                .filter(p -> p.getPastilleId() == null && "PROCESSED".equals(p.getProcessedStatus()))
                .toList();

        List<Object[]> duplicates = factPointageRepository.findPotentialDuplicates();

        Map<String, Object> integrity = Map.of(
                "orphanPointages", orphanPointages.size(),
                "potentialDuplicates", duplicates.size()
        );

        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Vérification d'intégrité terminée",
                integrity
        ), HttpStatus.OK);
    }
}