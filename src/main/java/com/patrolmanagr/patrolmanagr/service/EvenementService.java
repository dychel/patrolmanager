package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.*;
import com.patrolmanagr.patrolmanagr.dto.EvenementDTO;
import com.patrolmanagr.patrolmanagr.dto.IncidentDTO;
import com.patrolmanagr.patrolmanagr.entity.*;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.EvenementRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ExecRondeService execRondeService;

    @Autowired
    private ExecRondePastilleService execRondePastilleService;

    @Autowired
    private SysJobRunService sysJobRunService;

    @Autowired
    private IncidentService incidentService;

    public Evenement createEvenement(EvenementDTO evenementDTO) {
        Evenement evenement = modelMapper.map(evenementDTO, Evenement.class);

        if (evenementDTO.getExecRondeId() != null) {
            Exec_ronde execRonde = execRondeService.findExecRondeById(evenementDTO.getExecRondeId());
            evenement.setExecRonde(execRonde);
        }

        if (evenementDTO.getExecRondePastilleId() != null) {
            Exec_ronde_pastille pastille = execRondePastilleService.findExecRondePastilleById(evenementDTO.getExecRondePastilleId());
            evenement.setExecRondePastille(pastille);
        }

        if (evenementDTO.getJobRunId() != null) {
            SysJobRun jobRun = sysJobRunService.getJobRunById(evenementDTO.getJobRunId());
            evenement.setJobRun(jobRun);
        }

        evenement.setCreatedBy(userService.getConnectedUserId());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setStatus(EvenementStatus.NOUVEAU);

        Evenement savedEvenement = evenementRepository.save(evenement);

        checkAndEscalateToIncident(savedEvenement);

        return savedEvenement;
    }

    public Evenement createPositiveEvent(String title, String description, EvenementType type,
                                         Long execRondeId, Long pastilleId) {
        EvenementDTO dto = new EvenementDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setType(type);
        dto.setSeverity(EvenementSeverity.FAIBLE);
        dto.setStatus(EvenementStatus.NOUVEAU);
        dto.setExecRondeId(execRondeId);
        dto.setExecRondePastilleId(pastilleId);

        return createEvenement(dto);
    }

    public Evenement createAlertEvent(String title, String description, EvenementType type,
                                      EvenementSeverity severity, Long execRondeId, Long pastilleId) {
        EvenementDTO dto = new EvenementDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setType(type);
        dto.setSeverity(severity);
        dto.setStatus(EvenementStatus.NOUVEAU);
        dto.setExecRondeId(execRondeId);
        dto.setExecRondePastilleId(pastilleId);

        return createEvenement(dto);
    }

    private void checkAndEscalateToIncident(Evenement evenement) {
        boolean shouldEscalate = false;

        if (evenement.getSeverity() == EvenementSeverity.ELEVEE) {
            shouldEscalate = true;
        }

        if (evenement.getType() == EvenementType.OMISSION_PASTILLE) {
            shouldEscalate = true;
        }

        if (evenement.getType() == EvenementType.RONDE_INCOMPLETE &&
                evenement.getSeverity() == EvenementSeverity.ELEVEE) {
            shouldEscalate = true;
        }

        if (evenement.getType() == EvenementType.COLLECTE_ECHEC) {
            shouldEscalate = true;
        }

        if (evenement.getType() == EvenementType.IMMOBILISATION) {
            shouldEscalate = true;
        }

        if (shouldEscalate) {
            escalateToIncident(evenement);
        }
    }

    public void escalateToIncident(Evenement evenement) {
        IncidentDTO incidentDTO = new IncidentDTO();
        incidentDTO.setTitle("[ESCALADE] " + evenement.getTitle());
        incidentDTO.setDescription("Escaladé depuis événement: " + evenement.getDescription());
        incidentDTO.setType(convertEventTypeToIncidentType(evenement.getType()));
        incidentDTO.setSeverity(convertEventSeverityToIncidentSeverity(evenement.getSeverity()));
        incidentDTO.setStatus(IncidentStatus.OUVERT);

        if (evenement.getExecRonde() != null) {
            incidentDTO.setExecRondeId(evenement.getExecRonde().getId());
            incidentDTO.setSiteId(evenement.getExecRonde().getSite().getId());
            incidentDTO.setRondeId(evenement.getExecRonde().getRefRonde().getId());
        }

        if (evenement.getExecRondePastille() != null) {
            incidentDTO.setPastilleId(evenement.getExecRondePastille().getPastille().getId());
        }

        incidentDTO.setDelayMinutes(evenement.getDelayMinutes());

        incidentService.createIncident(incidentDTO);

        evenement.setStatus(EvenementStatus.ESCALADE);
        evenementRepository.save(evenement);
    }

    private IncidentType convertEventTypeToIncidentType(EvenementType eventType) {
        switch (eventType) {
            case PASTILLE_MANQUANTE:
            case OMISSION_PASTILLE:
                return IncidentType.PASTILLE_MANQUANTE;
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

    public Evenement resolveEvenement(Long id, String resolutionNotes) {
        Evenement evenement = getEvenementById(id);
        evenement.setStatus(EvenementStatus.TRAITE);
        evenement.setResolutionNotes(resolutionNotes);
        evenement.setResolvedAt(LocalDateTime.now());
        evenement.setResolvedBy(userService.getConnectedUserId());

        return evenementRepository.save(evenement);
    }

    public List<Evenement> getEvenementsByExecRonde(Long execRondeId) {
        return evenementRepository.findByExecRondeId(execRondeId);
    }

    public List<Evenement> getEvenementsByType(EvenementType type) {
        return evenementRepository.findByType(type);
    }

    public List<Evenement> getEvenementsBySeverity(EvenementSeverity severity) {
        return evenementRepository.findBySeverity(severity);
    }

    public List<Evenement> getEvenementsByStatus(EvenementStatus status) {
        return evenementRepository.findByStatus(status);
    }

    public List<Evenement> getActiveCriticalEvents() {
        return evenementRepository.findBySeverityAndStatus(EvenementSeverity.ELEVEE, EvenementStatus.NOUVEAU);
    }

    public Long countEvenementsByExecRondeAndType(Long execRondeId, EvenementType type) {
        return evenementRepository.countByExecRondeIdAndType(execRondeId, type);
    }

    public Long countRecentEventsBySite(Long siteId, int hours) {
        LocalDateTime startDate = LocalDateTime.now().minusHours(hours);
        return evenementRepository.countRecentEventsBySite(siteId, startDate);
    }

    // Méthode de recherche avec filtres
    public List<Evenement> searchEvenements(Long siteId, Long execRondeId, EvenementType type,
                                            EvenementSeverity severity, EvenementStatus status,
                                            LocalDateTime startDate, LocalDateTime endDate) {
        List<Evenement> allEvenements = evenementRepository.findAll();

        return allEvenements.stream()
                .filter(evenement -> siteId == null || (evenement.getSiteId() != null && evenement.getSiteId().equals(siteId)))
                .filter(evenement -> execRondeId == null || (evenement.getExecRonde() != null && evenement.getExecRonde().getId().equals(execRondeId)))
                .filter(evenement -> type == null || evenement.getType() == type)
                .filter(evenement -> severity == null || evenement.getSeverity() == severity)
                .filter(evenement -> status == null || evenement.getStatus() == status)
                .filter(evenement -> startDate == null || (evenement.getDetectedAt() != null && !evenement.getDetectedAt().isBefore(startDate)))
                .filter(evenement -> endDate == null || (evenement.getDetectedAt() != null && !evenement.getDetectedAt().isAfter(endDate)))
                .collect(Collectors.toList());
    }

    public Evenement getEvenementById(Long id) {
        return evenementRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Événement non trouvé"));
    }

    public List<Evenement> getAllEvenements() {
        return evenementRepository.findAll();
    }

    public Evenement updateEvenement(Long id, EvenementDTO evenementDTO) {
        Evenement existing = getEvenementById(id);
        modelMapper.map(evenementDTO, existing);
        return evenementRepository.save(existing);
    }

    public void deleteEvenement(Long id) {
        if (!evenementRepository.existsById(id)) {
            throw new ApiRequestException("Événement non trouvé");
        }
        evenementRepository.deleteById(id);
    }
}