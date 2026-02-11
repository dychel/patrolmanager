package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.*;
import com.patrolmanagr.patrolmanagr.dto.IncidentDTO;
import com.patrolmanagr.patrolmanagr.entity.*;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.IncidentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ExecRondeService execRondeService;

    @Autowired
    private ExecRondePastilleService execRondePastilleService;

    @Autowired
    private EvenementService evenementService;

    // Créer un incident
    public Incident createIncident(IncidentDTO incidentDTO) {
        Incident incident = modelMapper.map(incidentDTO, Incident.class);

        // Mettre à jour les clés étrangères
        if (incidentDTO.getExecRondeId() != null) {
            Exec_ronde execRonde = execRondeService.findExecRondeById(incidentDTO.getExecRondeId());
            incident.setExecRonde(execRonde);

            // Définir siteId et rondeId automatiquement
            if (incident.getSiteId() == null) {
                incident.setSiteId(execRonde.getSite().getId());
            }
            if (incident.getRondeId() == null) {
                incident.setRondeId(execRonde.getRefRonde().getId());
            }
        }

        if (incidentDTO.getExecRondePastilleId() != null) {
            Exec_ronde_pastille pastille = execRondePastilleService.findExecRondePastilleById(incidentDTO.getExecRondePastilleId());
            incident.setExecRondePastille(pastille);

            // Définir pastilleId automatiquement
            if (incident.getPastilleId() == null) {
                incident.setPastilleId(pastille.getPastille().getId());
            }
        }

        // Définir l'utilisateur créateur
        incident.setCreatedBy(userService.getConnectedUserId());
        incident.setCreatedAt(LocalDateTime.now());

        // Définir la date de détection si non fournie
        if (incident.getDetectedAt() == null) {
            incident.setDetectedAt(LocalDateTime.now());
        }

        // Initialiser le statut si non fourni
        if (incident.getStatus() == null) {
            incident.setStatus(IncidentStatus.OUVERT);
        }

        // Initialiser la sévérité si non fournie
        if (incident.getSeverity() == null) {
            incident.setSeverity(IncidentSeverity.MOYENNE);
        }

        return incidentRepository.save(incident);
    }

    // Créer un incident depuis un événement
    public Incident createIncidentFromEvent(Evenement evenement) {
        IncidentDTO incidentDTO = new IncidentDTO();
        incidentDTO.setTitle("[Depuis Événement] " + evenement.getTitle());
        incidentDTO.setDescription(evenement.getDescription());
        incidentDTO.setType(convertEventTypeToIncidentType(evenement.getType()));
        incidentDTO.setSeverity(convertEventSeverityToIncidentSeverity(evenement.getSeverity()));
        incidentDTO.setStatus(IncidentStatus.OUVERT);

        if (evenement.getExecRonde() != null) {
            incidentDTO.setExecRondeId(evenement.getExecRonde().getId());
        }

        if (evenement.getExecRondePastille() != null) {
            incidentDTO.setExecRondePastilleId(evenement.getExecRondePastille().getId());
        }

        incidentDTO.setSiteId(evenement.getSiteId());
        incidentDTO.setRondeId(evenement.getRondeId());
        incidentDTO.setPastilleId(evenement.getPastilleId());
        incidentDTO.setPointageId(evenement.getPointageId());
        incidentDTO.setAgentUserId(evenement.getAgentUserId());
        incidentDTO.setDelayMinutes(evenement.getDelayMinutes());
        incidentDTO.setExpectedTime(evenement.getExpectedTime());
        incidentDTO.setActualTime(evenement.getActualTime());
        incidentDTO.setDetectedAt(evenement.getDetectedAt());

        Incident incident = createIncident(incidentDTO);

        // Marquer l'événement comme escaladé
        evenement.setStatus(EvenementStatus.ESCALADE);
        evenementService.updateEvenement(evenement.getId(),
                modelMapper.map(evenement, com.patrolmanagr.patrolmanagr.dto.EvenementDTO.class));

        return incident;
    }

    // Méthodes de conversion
    private IncidentType convertEventTypeToIncidentType(EvenementType eventType) {
        switch (eventType) {
            case PASTILLE_MANQUANTE:
                return IncidentType.PASTILLE_MANQUANTE;
            case OMISSION_PASTILLE:
                return IncidentType.OMISSION_PASTILLE;
            case SEQUENCE_INCORRECTE:
                return IncidentType.SEQUENCE_INCORRECTE;
            case RETARD_MODERE:
            case RETARD_IMPORTANT:
                return IncidentType.RETARD;
            case TEMPS_TRAJET_ELEVE:
                return IncidentType.TEMPS_TRAJET_TROP_LONG;
            case DOUBLE_SCAN:
                return IncidentType.DOUBLE_SCAN;
            case HORS_PLAQUETTE:
                return IncidentType.HORS_PLAQUETTE;
            case RONDE_INCOMPLETE:
                return IncidentType.RONDE_INCOMPLETE;
            case TERMINAL_INACTIF:
                return IncidentType.TERMINAL_INACTIF;
            case COLLECTE_ECHEC:
                return IncidentType.COLLECTE_ECHEC;
            case IMMOBILISATION:
                return IncidentType.IMMOBILISATION;
            default:
                return IncidentType.AUTRE;
        }
    }

    private IncidentSeverity convertEventSeverityToIncidentSeverity(EvenementSeverity eventSeverity) {
        switch (eventSeverity) {
            case ELEVEE:
                return IncidentSeverity.ELEVEE;
            case MOYENNE:
                return IncidentSeverity.MOYENNE;
            case FAIBLE:
                return IncidentSeverity.FAIBLE;
            default:
                return IncidentSeverity.MOYENNE;
        }
    }

    // Résoudre un incident
    public Incident resolveIncident(Long id, String resolutionNotes) {
        Incident incident = getIncidentById(id);

        if (incident.getStatus() == IncidentStatus.RESOLU || incident.getStatus() == IncidentStatus.FERME) {
            throw new ApiRequestException("Cet incident est déjà résolu ou fermé");
        }

        incident.setStatus(IncidentStatus.RESOLU);
        incident.setResolutionNotes(resolutionNotes);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setResolvedBy(userService.getConnectedUserId());
        incident.setUpdatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    // Fermer un incident
    public Incident closeIncident(Long id) {
        Incident incident = getIncidentById(id);

        if (incident.getStatus() != IncidentStatus.RESOLU) {
            throw new ApiRequestException("L'incident doit être résolu avant d'être fermé");
        }

        incident.setStatus(IncidentStatus.FERME);
        incident.setUpdatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    // Mettre en cours un incident
    public Incident startProcessingIncident(Long id) {
        Incident incident = getIncidentById(id);

        if (incident.getStatus() == IncidentStatus.OUVERT) {
            incident.setStatus(IncidentStatus.EN_COURS);
            incident.setUpdatedAt(LocalDateTime.now());
        }

        return incidentRepository.save(incident);
    }

    // Réouvrir un incident
    public Incident reopenIncident(Long id, String reason) {
        Incident incident = getIncidentById(id);

        if (incident.getStatus() != IncidentStatus.RESOLU && incident.getStatus() != IncidentStatus.FERME) {
            throw new ApiRequestException("Seul un incident résolu ou fermé peut être réouvert");
        }

        incident.setStatus(IncidentStatus.REOPENED);
        incident.setResolutionNotes((incident.getResolutionNotes() != null ? incident.getResolutionNotes() + "\n" : "") +
                "Réouvert le " + LocalDateTime.now() + " - Raison: " + reason);
        incident.setUpdatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    // Mettre à jour la sévérité d'un incident
    public Incident updateSeverity(Long id, IncidentSeverity severity) {
        Incident incident = getIncidentById(id);
        incident.setSeverity(severity);
        incident.setUpdatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    // Mettre à jour le type d'un incident
    public Incident updateType(Long id, IncidentType type) {
        Incident incident = getIncidentById(id);
        incident.setType(type);
        incident.setUpdatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    // Assigner un agent à un incident
    public Incident assignAgent(Long id, Long agentUserId) {
        Incident incident = getIncidentById(id);
        incident.setAgentUserId(agentUserId);
        incident.setUpdatedAt(LocalDateTime.now());

        return incidentRepository.save(incident);
    }

    // Obtenir les incidents par exécution de ronde
    public List<Incident> getIncidentsByExecRonde(Long execRondeId) {
        return incidentRepository.findByExecRondeId(execRondeId);
    }

    // Obtenir les incidents par site
    public List<Incident> getIncidentsBySite(Long siteId) {
        return incidentRepository.findBySiteId(siteId);
    }

    // Obtenir les incidents par ronde
    public List<Incident> getIncidentsByRonde(Long rondeId) {
        return incidentRepository.findByRondeId(rondeId);
    }

    // Obtenir les incidents par pastille
    public List<Incident> getIncidentsByPastille(Long pastilleId) {
        return incidentRepository.findByPastilleId(pastilleId);
    }

    // Obtenir les incidents par type
    public List<Incident> getIncidentsByType(IncidentType type) {
        return incidentRepository.findByType(type);
    }

    // Obtenir les incidents par statut
    public List<Incident> getIncidentsByStatus(IncidentStatus status) {
        return incidentRepository.findByStatus(status);
    }

    // Obtenir les incidents par agent
    public List<Incident> getIncidentsByAgent(Long agentUserId) {
        return incidentRepository.findByAgentUserId(agentUserId);
    }

    // Obtenir les incidents par plage de dates
    public List<Incident> getIncidentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return incidentRepository.findByDetectedAtBetween(startDate, endDate);
    }

    // Obtenir les incidents par site et plage de dates
    public List<Incident> getIncidentsBySiteAndDateRange(Long siteId, LocalDateTime startDate, LocalDateTime endDate) {
        return incidentRepository.findBySiteIdAndDateRange(siteId, startDate, endDate);
    }

    // Obtenir les incidents par exécution de ronde et type
    public List<Incident> getIncidentsByExecRondeAndType(Long execRondeId, IncidentType type) {
        return incidentRepository.findByExecRondeIdAndType(execRondeId, type);
    }

    // Obtenir les incidents par pastille d'exécution
    public List<Incident> getIncidentsByExecRondePastille(Long execRondePastilleId) {
        return incidentRepository.findByExecRondePastilleId(execRondePastilleId);
    }

    // Obtenir les incidents par pointage
    public List<Incident> getIncidentsByPointage(Long pointageId) {
        return incidentRepository.findByPointageId(pointageId);
    }

    // Compter les incidents par exécution de ronde
    public Long countIncidentsByExecRonde(Long execRondeId) {
        return incidentRepository.countByExecRondeId(execRondeId);
    }

    // Obtenir les incidents prioritaires (ouverts et en cours)
    public List<Incident> getPriorityIncidents() {
        return incidentRepository.findByStatusInOrderByPriority(
                List.of(IncidentStatus.OUVERT, IncidentStatus.EN_COURS, IncidentStatus.REOPENED));
    }

    // Obtenir les statistiques des incidents
    public Map<String, Object> getIncidentStats(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new java.util.HashMap<>();

        // Obtenir tous les incidents dans la période
        List<Incident> allIncidents = incidentRepository.findByDetectedAtBetween(startDate, endDate);

        // Totaux
        stats.put("totalIncidents", allIncidents.size());

        // Par statut
        Map<IncidentStatus, Long> incidentsByStatus = allIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));
        stats.put("incidentsByStatus", incidentsByStatus);

        // Par type
        Map<IncidentType, Long> incidentsByType = allIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getType, Collectors.counting()));
        stats.put("incidentsByType", incidentsByType);

        // Par sévérité
        Map<IncidentSeverity, Long> incidentsBySeverity = allIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getSeverity, Collectors.counting()));
        stats.put("incidentsBySeverity", incidentsBySeverity);

        // Incidents ouverts
        long openIncidents = allIncidents.stream()
                .filter(i -> i.getStatus() == IncidentStatus.OUVERT ||
                        i.getStatus() == IncidentStatus.EN_COURS ||
                        i.getStatus() == IncidentStatus.REOPENED)
                .count();
        stats.put("openIncidents", openIncidents);

        // Temps moyen de résolution (pour les incidents résolus)
        double avgResolutionTime = allIncidents.stream()
                .filter(i -> i.getResolvedAt() != null && i.getDetectedAt() != null)
                .mapToLong(i -> java.time.Duration.between(i.getDetectedAt(), i.getResolvedAt()).toHours())
                .average()
                .orElse(0.0);
        stats.put("avgResolutionHours", avgResolutionTime);

        return stats;
    }

    // Obtenir les statistiques des incidents par site
    public Map<String, Object> getIncidentStatsBySite(Long siteId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new java.util.HashMap<>();

        List<Incident> siteIncidents = incidentRepository.findBySiteIdAndDateRange(siteId, startDate, endDate);

        stats.put("totalIncidents", siteIncidents.size());

        Map<IncidentStatus, Long> incidentsByStatus = siteIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));
        stats.put("incidentsByStatus", incidentsByStatus);

        Map<IncidentType, Long> incidentsByType = siteIncidents.stream()
                .collect(Collectors.groupingBy(Incident::getType, Collectors.counting()));
        stats.put("incidentsByType", incidentsByType);

        long openIncidents = siteIncidents.stream()
                .filter(i -> i.getStatus() == IncidentStatus.OUVERT ||
                        i.getStatus() == IncidentStatus.EN_COURS ||
                        i.getStatus() == IncidentStatus.REOPENED)
                .count();
        stats.put("openIncidents", openIncidents);

        return stats;
    }

    // Rechercher des incidents avec filtres
    public List<Incident> searchIncidents(Long siteId, Long rondeId, IncidentType type,
                                          IncidentStatus status, LocalDateTime startDate,
                                          LocalDateTime endDate) {
        List<Incident> allIncidents = incidentRepository.findAll();

        return allIncidents.stream()
                .filter(incident -> siteId == null || (incident.getSiteId() != null && incident.getSiteId().equals(siteId)))
                .filter(incident -> rondeId == null || (incident.getRondeId() != null && incident.getRondeId().equals(rondeId)))
                .filter(incident -> type == null || incident.getType() == type)
                .filter(incident -> status == null || incident.getStatus() == status)
                .filter(incident -> startDate == null || (incident.getDetectedAt() != null && !incident.getDetectedAt().isBefore(startDate)))
                .filter(incident -> endDate == null || (incident.getDetectedAt() != null && !incident.getDetectedAt().isAfter(endDate)))
                .collect(Collectors.toList());
    }

    // Méthodes CRUD standards
    public Incident getIncidentById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Incident non trouvé avec ID: " + id));
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public Incident updateIncident(Long id, IncidentDTO incidentDTO) {
        Incident existing = getIncidentById(id);

        Long originalCreatedBy = existing.getCreatedBy();
        LocalDateTime originalCreatedAt = existing.getCreatedAt();

        modelMapper.map(incidentDTO, existing);

        existing.setCreatedBy(originalCreatedBy);
        existing.setCreatedAt(originalCreatedAt);
        existing.setUpdatedAt(LocalDateTime.now());

        if (incidentDTO.getExecRondeId() != null &&
                (existing.getExecRonde() == null || !existing.getExecRonde().getId().equals(incidentDTO.getExecRondeId()))) {
            Exec_ronde execRonde = execRondeService.findExecRondeById(incidentDTO.getExecRondeId());
            existing.setExecRonde(execRonde);
        }

        if (incidentDTO.getExecRondePastilleId() != null &&
                (existing.getExecRondePastille() == null || !existing.getExecRondePastille().getId().equals(incidentDTO.getExecRondePastilleId()))) {
            Exec_ronde_pastille pastille = execRondePastilleService.findExecRondePastilleById(incidentDTO.getExecRondePastilleId());
            existing.setExecRondePastille(pastille);
        }

        return incidentRepository.save(existing);
    }

    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new ApiRequestException("Incident non trouvé avec ID: " + id);
        }
        incidentRepository.deleteById(id);
    }

    // Méthode pour supprimer les incidents anciens
    public void deleteOldIncidents(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<Incident> oldIncidents = incidentRepository.findByDetectedAtBetween(
                LocalDateTime.MIN, cutoffDate);

        List<Incident> incidentsToDelete = oldIncidents.stream()
                .filter(i -> i.getStatus() == IncidentStatus.FERME)
                .collect(Collectors.toList());

        incidentRepository.deleteAll(incidentsToDelete);
    }

    // Méthode pour archiver les incidents résolus
    public void archiveResolvedIncidents() {
        List<Incident> resolvedIncidents = incidentRepository.findByStatus(IncidentStatus.RESOLU);

        LocalDateTime archiveDate = LocalDateTime.now().minusDays(30);
        for (Incident incident : resolvedIncidents) {
            if (incident.getResolvedAt() != null && incident.getResolvedAt().isBefore(archiveDate)) {
                incident.setStatus(IncidentStatus.FERME);
                incidentRepository.save(incident);
            }
        }
    }
}