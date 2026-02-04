package com.patrolmanagr.patrolmanagr.dto;

import com.patrolmanagr.patrolmanagr.entity.Exec_ronde_pastille;
import com.patrolmanagr.patrolmanagr.entity.Incident;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RondeExecutionReportDTO {
    private ExecRondeDTO execRonde;
    private List<Exec_ronde_pastille> execPastilles;
    private List<Incident> incidents;

    // Statistiques
    private int totalPastilles;
    private int donePastilles;
    private int missedPastilles;
    private int expectedPastilles;
    private double completionRate;
    private int totalIncidents;
    private Map<String, Long> incidentsByType;
    private int totalDelayMinutes;
    private double avgDelayMinutes;
    private List<String> majorIssues;
}