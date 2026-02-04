package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.dto.ExecRondeDTO;
import com.patrolmanagr.patrolmanagr.dto.RondeExecutionReportDTO;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde_pastille;
import com.patrolmanagr.patrolmanagr.entity.Incident;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.ExecRondeRepository;
import com.patrolmanagr.patrolmanagr.repository.ExecRondePastilleRepository;
import com.patrolmanagr.patrolmanagr.repository.IncidentRepository;
import com.patrolmanagr.patrolmanagr.repository.RefRondeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RondeExecutionQueryService {

    @Autowired
    private ExecRondeRepository execRondeRepository;

    @Autowired
    private ExecRondePastilleRepository execRondePastilleRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private RefRondeRepository refRondeRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Récupérer toutes les rondes exécutées
     */
    public List<ExecRondeDTO> getAllExecutedRondes() {
        List<Exec_ronde> execRondes = execRondeRepository.findAll();
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes exécutées par date
     */
    public List<ExecRondeDTO> getExecutedRondesByDate(LocalDate date) {
        List<Exec_ronde> execRondes = execRondeRepository.findByExecDate(date);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes exécutées par site et date
     */
    public List<ExecRondeDTO> getExecutedRondesBySiteAndDate(Long siteId, LocalDate date) {
        List<Exec_ronde> execRondes = execRondeRepository.findBySiteIdAndExecDate(siteId, date);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes exécutées par site sur une période
     */
    public List<ExecRondeDTO> getExecutedRondesBySiteAndPeriod(Long siteId, LocalDate startDate, LocalDate endDate) {
        List<Exec_ronde> execRondes = execRondeRepository.findBySiteIdAndDateRange(siteId, startDate, endDate);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes exécutées par statut
     */
    public List<ExecRondeDTO> getExecutedRondesByStatus(Status_exec_Ronde status) {
        List<Exec_ronde> execRondes = execRondeRepository.findByStatus(status);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes exécutées pour une ronde spécifique
     */
    public List<ExecRondeDTO> getExecutedRondesForRonde(Long rondeId) {
        Ref_ronde refRonde = refRondeRepository.findById(rondeId)
                .orElseThrow(() -> new ApiRequestException("Ronde non trouvée"));

        List<Exec_ronde> execRondes = execRondeRepository.findByRefRondeId(rondeId);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes exécutées aujourd'hui
     */
    public List<ExecRondeDTO> getTodayExecutedRondes() {
        LocalDate today = LocalDate.now();
        return getExecutedRondesByDate(today);
    }

    /**
     * Récupérer les rondes en cours d'exécution
     */
    public List<ExecRondeDTO> getInProgressRondes() {
        List<Exec_ronde> execRondes = execRondeRepository.findByStatus(Status_exec_Ronde.IN_PROGRESS);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes terminées
     */
    public List<ExecRondeDTO> getCompletedRondes() {
        List<Exec_ronde> execRondes = execRondeRepository.findByStatus(Status_exec_Ronde.DONE);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les détails d'une ronde exécutée
     */
    public RondeExecutionReportDTO getRondeExecutionDetails(Long execRondeId) {
        Exec_ronde execRonde = execRondeRepository.findById(execRondeId)
                .orElseThrow(() -> new ApiRequestException("Exécution de ronde non trouvée"));

        // Récupérer les pastilles exécutées
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeId(execRondeId);

        // Récupérer les incidents
        List<Incident> incidents = incidentRepository.findByExecRondeId(execRondeId);

        // Construire le rapport
        RondeExecutionReportDTO report = new RondeExecutionReportDTO();
        report.setExecRonde(convertToDTO(execRonde));
        report.setExecPastilles(execPastilles);
        report.setIncidents(incidents);

        // Calculer les statistiques
        calculateStatistics(report, execPastilles, incidents);

        return report;
    }

    /**
     * Récupérer l'historique des exécutions pour une ronde
     */
    public List<RondeExecutionReportDTO> getRondeExecutionHistory(Long rondeId) {
        List<Exec_ronde> execRondes = execRondeRepository.findByRefRondeId(rondeId);

        return execRondes.stream()
                .map(execRonde -> getRondeExecutionDetails(execRonde.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les rondes exécutées par un job run
     */
    public List<ExecRondeDTO> getExecutedRondesByJobRun(Long jobRunId) {
        List<Exec_ronde> execRondes = execRondeRepository.findByJobRunId(jobRunId);
        return execRondes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des rondes exécutées
     */
    public List<ExecRondeDTO> searchExecutedRondes(
            Long siteId,
            Long rondeId,
            LocalDate startDate,
            LocalDate endDate,
            Status_exec_Ronde status) {

        List<Exec_ronde> execRondes = execRondeRepository.findAll();

        // Filtrer selon les critères
        return execRondes.stream()
                .filter(er -> siteId == null || (er.getSite() != null && er.getSite().getId().equals(siteId)))
                .filter(er -> rondeId == null || (er.getRefRonde() != null && er.getRefRonde().getId().equals(rondeId)))
                .filter(er -> startDate == null || !er.getExecDate().isBefore(startDate))
                .filter(er -> endDate == null || !er.getExecDate().isAfter(endDate))
                .filter(er -> status == null || er.getStatus() == status)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques d'exécution pour un site
     */
    public Map<String, Object> getSiteExecutionStats(Long siteId, LocalDate startDate, LocalDate endDate) {
        List<Exec_ronde> execRondes = execRondeRepository.findBySiteIdAndDateRange(siteId, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRondes", execRondes.size());

        // Compter par statut
        long planned = execRondes.stream().filter(er -> er.getStatus() == Status_exec_Ronde.PLANNED).count();
        long inProgress = execRondes.stream().filter(er -> er.getStatus() == Status_exec_Ronde.IN_PROGRESS).count();
        long done = execRondes.stream().filter(er -> er.getStatus() == Status_exec_Ronde.DONE).count();
        long cancelled = execRondes.stream().filter(er -> er.getStatus() == Status_exec_Ronde.CANCELLED).count();

        stats.put("planned", planned);
        stats.put("inProgress", inProgress);
        stats.put("done", done);
        stats.put("cancelled", cancelled);

        // Calculer le taux de complétion moyen
        OptionalDouble avgCompletion = execRondes.stream()
                .filter(er -> er.getCompletionRate() != null)
                .mapToDouble(er -> er.getCompletionRate().doubleValue())
                .average();

        stats.put("avgCompletionRate", avgCompletion.isPresent() ? avgCompletion.getAsDouble() : 0.0);

        // Compter les incidents
        long totalIncidents = 0;
        long totalRetards = 0;
        long totalPastillesManquantes = 0;

        for (Exec_ronde execRonde : execRondes) {
            List<Incident> incidents = incidentRepository.findByExecRondeId(execRonde.getId());
            totalIncidents += incidents.size();

            totalRetards += incidents.stream()
                    .filter(i -> i.getType().toString().contains("RETARD"))
                    .count();

            totalPastillesManquantes += incidents.stream()
                    .filter(i -> i.getType().toString().contains("PASTILLE_MANQUANTE"))
                    .count();
        }

        stats.put("totalIncidents", totalIncidents);
        stats.put("totalRetards", totalRetards);
        stats.put("totalPastillesManquantes", totalPastillesManquantes);

        // Top 5 des rondes avec le plus d'incidents
        List<Map<String, Object>> topRondesWithIncidents = execRondes.stream()
                .map(er -> {
                    long incidentCount = incidentRepository.countByExecRondeId(er.getId());
                    Map<String, Object> rondeStats = new HashMap<>();
                    rondeStats.put("rondeId", er.getRefRonde().getId());
                    rondeStats.put("rondeName", er.getRefRonde().getCode());
                    rondeStats.put("execDate", er.getExecDate());
                    rondeStats.put("incidentCount", incidentCount);
                    return rondeStats;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("incidentCount"), (Long) a.get("incidentCount")))
                .limit(5)
                .collect(Collectors.toList());

        stats.put("topRondesWithIncidents", topRondesWithIncidents);

        return stats;
    }

    /**
     * Convertir Exec_ronde en DTO
     */
    private ExecRondeDTO convertToDTO(Exec_ronde execRonde) {
        ExecRondeDTO dto = modelMapper.map(execRonde, ExecRondeDTO.class);

        // Mapper les IDs
        if (execRonde.getRefRonde() != null) {
            dto.setRefRondeId(execRonde.getRefRonde().getId());
            dto.setRondeCode(execRonde.getRefRonde().getCode());
            dto.setRondeName(execRonde.getRefRonde().getCode()); // ou un champ name si disponible
        }

        if (execRonde.getSite() != null) {
            dto.setSiteId(execRonde.getSite().getId());
            dto.setSiteName(execRonde.getSite().getName());
        }

        if (execRonde.getJobRun() != null) {
            dto.setJobRunId(execRonde.getJobRun().getId());
        }

        // Compter les incidents
        Long incidentCount = incidentRepository.countByExecRondeId(execRonde.getId());
        dto.setIncidentCount(incidentCount != null ? incidentCount.intValue() : 0);

        return dto;
    }

    /**
     * Calculer les statistiques pour un rapport
     */
    private void calculateStatistics(RondeExecutionReportDTO report,
                                     List<Exec_ronde_pastille> execPastilles,
                                     List<Incident> incidents) {

        // Statistiques des pastilles
        long totalPastilles = execPastilles.size();
        long donePastilles = execPastilles.stream()
                .filter(p -> p.getStatus().toString().equals("DONE"))
                .count();
        long missedPastilles = execPastilles.stream()
                .filter(p -> p.getStatus().toString().equals("MISSED"))
                .count();
        long expectedPastilles = execPastilles.stream()
                .filter(p -> p.getStatus().toString().equals("EXPECTED"))
                .count();

        report.setTotalPastilles((int) totalPastilles);
        report.setDonePastilles((int) donePastilles);
        report.setMissedPastilles((int) missedPastilles);
        report.setExpectedPastilles((int) expectedPastilles);

        // Taux de complétion
        if (totalPastilles > 0) {
            double completionRate = (donePastilles * 100.0) / totalPastilles;
            report.setCompletionRate(completionRate);
        } else {
            report.setCompletionRate(0.0);
        }

        // Statistiques des incidents
        report.setTotalIncidents(incidents.size());

        // Grouper les incidents par type
        Map<String, Long> incidentsByType = incidents.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getType().toString(),
                        Collectors.counting()
                ));
        report.setIncidentsByType(incidentsByType);

        // Calculer le temps total de retard
        int totalDelayMinutes = incidents.stream()
                .filter(i -> i.getDelayMinutes() != null)
                .mapToInt(Incident::getDelayMinutes)
                .sum();
        report.setTotalDelayMinutes(totalDelayMinutes);

        // Temps moyen de retard
        long delayIncidents = incidents.stream()
                .filter(i -> i.getDelayMinutes() != null && i.getDelayMinutes() > 0)
                .count();

        if (delayIncidents > 0) {
            report.setAvgDelayMinutes(totalDelayMinutes / (double) delayIncidents);
        } else {
            report.setAvgDelayMinutes(0.0);
        }

        // Détecter les problèmes majeurs
        List<String> majorIssues = new ArrayList<>();

        if (missedPastilles > 0) {
            majorIssues.add(missedPastilles + " pastille(s) manquante(s)");
        }

        if (totalDelayMinutes > 30) { // Plus de 30 minutes de retard total
            majorIssues.add("Retard important: " + totalDelayMinutes + " minutes");
        }

        long sequenceErrors = incidents.stream()
                .filter(i -> i.getType().toString().contains("SEQUENCE"))
                .count();

        if (sequenceErrors > 0) {
            majorIssues.add(sequenceErrors + " erreur(s) de séquence");
        }

        report.setMajorIssues(majorIssues);
    }
}