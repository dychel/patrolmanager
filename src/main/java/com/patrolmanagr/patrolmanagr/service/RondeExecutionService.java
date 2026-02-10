package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.*;
import com.patrolmanagr.patrolmanagr.entity.*;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RondeExecutionService {

    @Autowired
    private SysJobRunRepository sysJobRunRepository;

    @Autowired
    private ExecRondeRepository execRondeRepository;

    @Autowired
    private ExecRondePastilleRepository execRondePastilleRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private FactPointageRepository factPointageRepository;

    @Autowired
    private RefRondeRepository refRondeRepository;

    @Autowired
    private RefPastilleRepository refPastilleRepository;

    @Autowired
    private RefRondePastilleRepository refRondePastilleRepository;

    @Autowired
    private SysJobRepository sysJobRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    /**
     * Exécuter un job pour créer des rondes et détecter des incidents
     */
    public SysJobRun executeJob(Long jobId) {
        // Récupérer le job
        SysJob job = sysJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        // Démarrer le job run
        SysJobRun jobRun = startJobRun(job);

        try {
            // Récupérer les rondes à exécuter
            List<Ref_ronde> rondes = getRondesForJob(job);

            int totalRondes = 0;
            int totalPointages = 0;
            int totalIncidents = 0;

            // Exécuter chaque ronde
            for (Ref_ronde ronde : rondes) {
                try {
                    Exec_ronde execRonde = executeRonde(ronde, jobRun);
                    totalRondes++;

                    // Analyser les pointages pour cette ronde
                    int pointages = analyzePointagesForRonde(execRonde);
                    totalPointages += pointages;

                    // Détecter les incidents
                    int incidents = detectIncidentsForRonde(execRonde);
                    totalIncidents += incidents;

                } catch (Exception e) {
                    // Log l'erreur mais continue avec les autres rondes
                    System.err.println("Erreur lors de l'exécution de la ronde " + ronde.getId() + ": " + e.getMessage());
                }
            }

            // Mettre à jour les statistiques du job run
            updateJobRunStats(jobRun, totalRondes, totalPointages, totalIncidents);

            // Terminer le job run avec succès
            return completeJobRun(jobRun, JobRunStatus.OK,
                    String.format("Exécution terminée: %d rondes, %d pointages, %d incidents",
                            totalRondes, totalPointages, totalIncidents));

        } catch (Exception e) {
            // Erreur générale
            return completeJobRun(jobRun, JobRunStatus.ERROR,
                    "Erreur lors de l'exécution: " + e.getMessage());
        }
    }

    /**
     * Démarrer un job run
     */
    private SysJobRun startJobRun(SysJob job) {
        SysJobRun jobRun = new SysJobRun();
        jobRun.setJob(job);
        jobRun.setStartedAt(LocalDateTime.now());
        jobRun.setStatus(JobRunStatus.RUNNING);
        jobRun.setMessage("Démarrage de l'exécution");

        return sysJobRunRepository.save(jobRun);
    }

    /**
     * Terminer un job run
     */
    private SysJobRun completeJobRun(SysJobRun jobRun, JobRunStatus status, String message) {
        jobRun.setEndedAt(LocalDateTime.now());
        jobRun.setStatus(status);
        jobRun.setMessage(message);

        // Calculer la durée
        if (jobRun.getStartedAt() != null && jobRun.getEndedAt() != null) {
            Duration duration = Duration.between(jobRun.getStartedAt(), jobRun.getEndedAt());
            jobRun.setDurationMs(duration.toMillis());
        }

        return sysJobRunRepository.save(jobRun);
    }

    /**
     * Mettre à jour les statistiques du job run
     */
    private void updateJobRunStats(SysJobRun jobRun, int rondeCount, int pointageCount, int incidentCount) {
        jobRun.setRondeCount(rondeCount);
        jobRun.setPointageCount(pointageCount);
        jobRun.setIncidentCount(incidentCount);
        sysJobRunRepository.save(jobRun);
    }

    /**
     * Récupérer les rondes pour un job
     */
    private List<Ref_ronde> getRondesForJob(SysJob job) {
        List<Ref_ronde> rondes = new ArrayList<>();

        // Récupérer les IDs des rondes spécifiées dans le job
        if (job.getRondeIds() != null && !job.getRondeIds().isEmpty()) {
            List<Long> rondeIds = parseRondeIds(job.getRondeIds());
            for (Long rondeId : rondeIds) {
                try {
                    Ref_ronde ronde = refRondeRepository.findByIdRonde(rondeId);
                    //.orElseThrow(() -> new ApiRequestException("Ronde non trouvée: " + rondeId));
                    //if (ronde.getStatus() == Status.ACTIVE) {
                    //if (ronde.getCode() != null) {
                        rondes.add(ronde);
                    //}
                } catch (Exception e) {
                    // Log et continue
                    System.err.println("Ronde " + rondeId + " non trouvée ou inactive");
                }
            }
        } else {
            // Si pas de rondes spécifiées, utiliser le scope du job
            switch (job.getJobScope()) {
                case GLOBAL:
                    // Toutes les rondes actives
                    rondes = refRondeRepository.findAll()
                            .stream()
                            .filter(r -> r.getStatus() == Status.ACTIVE)
                            .collect(Collectors.toList());
                    break;

                case ALL_SITES:
                    // Rondes de tous les sites
                    rondes = refRondeRepository.findAll()
                            .stream()
                            .filter(r -> r.getStatus() == Status.ACTIVE)
                            .collect(Collectors.toList());
                    break;

                case SPECIFIC_SITE:
                    // TODO: Implémenter si besoin de sites spécifiques
                    // Pour l'instant, toutes les rondes actives
                    rondes = refRondeRepository.findAll()
                            .stream()
                            .filter(r -> r.getStatus() == Status.ACTIVE)
                            .collect(Collectors.toList());
                    break;
            }
        }

        return rondes;
    }

    /**
     * Exécuter une ronde
     */
    private Exec_ronde executeRonde(Ref_ronde ronde, SysJobRun jobRun) {
        // Vérifier si une exécution existe déjà pour aujourd'hui
        LocalDate today = LocalDate.now();
        List<Exec_ronde> existingExecutions = execRondeRepository
                .findByRondeIdAndDate(ronde.getId(), today);

        if (!existingExecutions.isEmpty()) {
            // Mettre à jour l'exécution existante
            Exec_ronde existing = existingExecutions.get(0);
            existing.setJobRun(jobRun);
            existing.setUpdated_at(LocalDateTime.now());
            existing.setUpdated_by(userService.getConnectedUserId());
            return execRondeRepository.save(existing);
        }

        // Créer une nouvelle exécution
        Exec_ronde execRonde = new Exec_ronde();
        execRonde.setRefRonde(ronde);
        execRonde.setSite(ronde.getRef_site());
        execRonde.setExecDate(today);
        execRonde.setStatus(Status_exec_Ronde.IN_PROGRESS);
        execRonde.setJobRun(jobRun);

        // Déterminer les heures de début et fin
        LocalDateTime plannedStart = calculatePlannedStart(ronde);
        LocalDateTime plannedEnd = calculatePlannedEnd(ronde, plannedStart);

        execRonde.setPlannedStartAt(plannedStart);
        execRonde.setPlannedEndAt(plannedEnd);
        execRonde.setStartedAt(LocalDateTime.now());
        execRonde.setCreated_at(LocalDateTime.now());
        execRonde.setCreated_by(userService.getConnectedUserId());

        // Créer les pastilles d'exécution
        execRonde = execRondeRepository.save(execRonde);
        createExecRondePastilles(execRonde);

        return execRonde;
    }

    /**
     * Calculer l'heure de début prévue
     */
    private LocalDateTime calculatePlannedStart(Ref_ronde ronde) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime heureDebut = ronde.getHeure_debut();

        if (heureDebut != null) {
            return LocalDateTime.of(now.toLocalDate(), heureDebut);
        }

        // Par défaut, maintenant
        return now;
    }

    /**
     * Calculer l'heure de fin prévue
     */
    private LocalDateTime calculatePlannedEnd(Ref_ronde ronde, LocalDateTime plannedStart) {
        Integer dureeMinutes = ronde.getExpected_duration_min();

        if (dureeMinutes != null && dureeMinutes > 0) {
            return plannedStart.plusMinutes(dureeMinutes);
        }

        // Par défaut, 1 heure
        return plannedStart.plusHours(1);
    }

    /**
     * Créer les pastilles d'exécution
     */
    private void createExecRondePastilles(Exec_ronde execRonde) {
        // Récupérer les pastilles de la ronde
        List<Ref_ronde_pastille> refPastilles = refRondePastilleRepository
                .findByRondeIdOrderBySequence(execRonde.getRefRonde().getId());

        if (refPastilles.isEmpty()) {
            return;
        }

        LocalDateTime currentExpectedTime = execRonde.getPlannedStartAt();

        for (Ref_ronde_pastille refPastille : refPastilles) {
            Exec_ronde_pastille execPastille = new Exec_ronde_pastille();
            execPastille.setExecRonde(execRonde);
            execPastille.setPastille(refPastille.getRef_pastille_id());
            execPastille.setSeqNo(refPastille.getSeq_no());
            execPastille.setExpectedTravelSec(refPastille.getExpected_travel_sec());
            execPastille.setStatus(Status_ronde_pastille.EXPECTED);
            execPastille.setExpectedTime(currentExpectedTime);
            execPastille.setCreated_at(LocalDateTime.now());
            execPastille.setCreated_by(userService.getConnectedUserId());

            // Calculer le temps prévu pour la prochaine pastille
            if (refPastille.getExpected_travel_sec() != null) {
                currentExpectedTime = currentExpectedTime.plusSeconds(refPastille.getExpected_travel_sec());
            } else {
                // Temps par défaut : 5 minutes
                currentExpectedTime = currentExpectedTime.plusMinutes(5);
            }

            execRondePastilleRepository.save(execPastille);
        }
    }

    /**
     * Analyser les pointages pour une ronde
     */
    private int analyzePointagesForRonde(Exec_ronde execRonde) {
        // Récupérer les pointages pour le site et la période de la ronde
        LocalDate execDate = execRonde.getExecDate();
        LocalDateTime startOfDay = execDate.atStartOfDay();
        LocalDateTime endOfDay = execDate.atTime(LocalTime.MAX);

        List<Fact_pointage> pointages = factPointageRepository
                .findBySiteIdAndEventTimeBetween(
                        execRonde.getSite().getId(),
                        startOfDay,
                        endOfDay
                );

        // Filtrer par ronde si disponible
        pointages = pointages.stream()
                .filter(p -> p.getRondeId() != null && p.getRondeId().equals(execRonde.getRefRonde().getId()))
                .collect(Collectors.toList());

        // Associer les pointages aux pastilles
        for (Fact_pointage pointage : pointages) {
            associatePointageWithPastille(pointage, execRonde);
        }

        return pointages.size();
    }

    /**
     * Associer un pointage à une pastille
     */
    private void associatePointageWithPastille(Fact_pointage pointage, Exec_ronde execRonde) {
        if (pointage.getPastilleId() == null) {
            return;
        }

        // Trouver la pastille d'exécution correspondante
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeIdAndPastilleId(execRonde.getId(), pointage.getPastilleId());

        if (!execPastilles.isEmpty()) {
            Exec_ronde_pastille execPastille = execPastilles.get(0);

            // Vérifier si déjà scannée
            if (execPastille.getStatus() == Status_ronde_pastille.DONE) {
                // Détecter un double scan
                detectDoubleScanIncident(execPastille, pointage);
                return;
            }

            // Mettre à jour la pastille d'exécution
            updateExecPastilleFromPointage(execPastille, pointage);

            // Vérifier la séquence
            checkSequence(execPastille, execRonde);

            // Vérifier les retards
            checkDelays(execPastille);

            // Vérifier le temps de trajet
            checkTravelTime(execPastille, execRonde);
        } else {
            // Pastille non attendue dans cette ronde
            detectUnexpectedPastilleIncident(pointage, execRonde);
        }
    }

    /**
     * Mettre à jour une pastille d'exécution à partir d'un pointage
     */
    private void updateExecPastilleFromPointage(Exec_ronde_pastille execPastille, Fact_pointage pointage) {
        execPastille.setStatus(Status_ronde_pastille.DONE);
        execPastille.setScannedAt(pointage.getEventTime());
        execPastille.setActualTime(pointage.getEventTime());
        execPastille.setPointageId(pointage.getId());
        execPastille.setUpdated_at(LocalDateTime.now());
        execPastille.setUpdated_by(userService.getConnectedUserId());

        // Calculer la déviation
        if (execPastille.getExpectedTime() != null) {
            Duration deviation = Duration.between(execPastille.getExpectedTime(), pointage.getEventTime());
            execPastille.setDeviationSec((int) deviation.getSeconds());
            execPastille.setIsLate(deviation.getSeconds() > 0);
            execPastille.setLateMinutes((int) deviation.toMinutes());
        }

        execRondePastilleRepository.save(execPastille);
    }

    /**
     * Détecter les incidents pour une ronde
     */
    private int detectIncidentsForRonde(Exec_ronde execRonde) {
        int incidentCount = 0;

        // Récupérer toutes les pastilles d'exécution
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeId(execRonde.getId());

        // 1. Vérifier les pastilles manquantes
        for (Exec_ronde_pastille execPastille : execPastilles) {
            if (execPastille.getStatus() == Status_ronde_pastille.EXPECTED ||
                    execPastille.getStatus() == Status_ronde_pastille.MISSED) {
                // Pastille manquante
                createMissingPastilleIncident(execPastille);
                incidentCount++;
            }
        }

        // 2. Vérifier les retards globaux
        checkGlobalDelays(execRonde, execPastilles);

        // 3. Mettre à jour le taux de complétion
        updateCompletionRate(execRonde, execPastilles);

        // 4. Compter les incidents existants
        List<Evenement> existingEvenements = incidentRepository.findByExecRondeId(execRonde.getId());
        incidentCount += existingEvenements.size();

        // Mettre à jour le compteur d'incidents
        execRonde.setIncidentCount(incidentCount);
        execRondeRepository.save(execRonde);

        return incidentCount;
    }

    /**
     * Créer un incident de pastille manquante
     */
    private void createMissingPastilleIncident(Exec_ronde_pastille execPastille) {
        Evenement evenement = new Evenement();
        evenement.setType(IncidentType.PASTILLE_MANQUANTE);
        evenement.setSeverity(IncidentSeverity.MEDIUM);
        evenement.setStatus(IncidentStatus.OPEN);
        evenement.setTitle("Pastille manquante: " + execPastille.getPastille().getCode());
        evenement.setDescription("La pastille n'a pas été scannée pendant la ronde");
        evenement.setExecRonde(execPastille.getExecRonde());
        evenement.setExecRondePastille(execPastille);
        evenement.setSiteId(execPastille.getExecRonde().getSite().getId());
        evenement.setRondeId(execPastille.getExecRonde().getRefRonde().getId());
        evenement.setPastilleId(execPastille.getPastille().getId());
        evenement.setDetectedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setCreatedBy(userService.getConnectedUserId());

        incidentRepository.save(evenement);
    }

    /**
     * Détecter un double scan
     */
    private void detectDoubleScanIncident(Exec_ronde_pastille execPastille, Fact_pointage pointage) {
        Evenement evenement = new Evenement();
        evenement.setType(IncidentType.DOUBLE_SCAN);
        evenement.setSeverity(IncidentSeverity.LOW);
        evenement.setStatus(IncidentStatus.OPEN);
        evenement.setTitle("Double scan détecté");
        evenement.setDescription("La pastille " + execPastille.getPastille().getCode() + " a été scannée plusieurs fois");
        evenement.setExecRonde(execPastille.getExecRonde());
        evenement.setExecRondePastille(execPastille);
        evenement.setSiteId(execPastille.getExecRonde().getSite().getId());
        evenement.setRondeId(execPastille.getExecRonde().getRefRonde().getId());
        evenement.setPastilleId(execPastille.getPastille().getId());
        evenement.setPointageId(pointage.getId());
        evenement.setAgentUserId(pointage.getAgentUserId());
        evenement.setDetectedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setCreatedBy(userService.getConnectedUserId());

        incidentRepository.save(evenement);
    }

    /**
     * Détecter une pastille non attendue
     */
    private void detectUnexpectedPastilleIncident(Fact_pointage pointage, Exec_ronde execRonde) {
        Evenement evenement = new Evenement();
        evenement.setType(IncidentType.HORS_PLAQUETTE);
        evenement.setSeverity(IncidentSeverity.MEDIUM);
        evenement.setStatus(IncidentStatus.OPEN);
        evenement.setTitle("Pastille non attendue");
        evenement.setDescription("Pastille scannée hors de la ronde prévue");
        evenement.setExecRonde(execRonde);
        evenement.setSiteId(execRonde.getSite().getId());
        evenement.setRondeId(execRonde.getRefRonde().getId());
        evenement.setPastilleId(pointage.getPastilleId());
        evenement.setPointageId(pointage.getId());
        evenement.setAgentUserId(pointage.getAgentUserId());
        evenement.setDetectedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setCreatedBy(userService.getConnectedUserId());

        incidentRepository.save(evenement);
    }

    /**
     * Vérifier la séquence
     */
    private void checkSequence(Exec_ronde_pastille currentPastille, Exec_ronde execRonde) {
        List<Exec_ronde_pastille> allPastilles = execRondePastilleRepository
                .findByExecRondeIdOrderBySeqNo(execRonde.getId());

        // Trouver l'index de la pastille actuelle
        int currentIndex = -1;
        for (int i = 0; i < allPastilles.size(); i++) {
            if (allPastilles.get(i).getId().equals(currentPastille.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            // Vérifier si la pastille précédente a été scannée
            Exec_ronde_pastille previousPastille = allPastilles.get(currentIndex - 1);
            if (previousPastille.getStatus() != Status_ronde_pastille.DONE) {
                // Séquence incorrecte
                createSequenceErrorIncident(currentPastille, previousPastille);
            }
        }
    }

    /**
     * Créer un incident d'erreur de séquence
     */
    private void createSequenceErrorIncident(Exec_ronde_pastille currentPastille, Exec_ronde_pastille missingPastille) {
        Evenement evenement = new Evenement();
        evenement.setType(IncidentType.SEQUENCE_INCORRECTE);
        evenement.setSeverity(IncidentSeverity.MEDIUM);
        evenement.setStatus(IncidentStatus.OPEN);
        evenement.setTitle("Erreur de séquence");
        evenement.setDescription("La pastille " + currentPastille.getPastille().getCode() +
                " a été scannée avant la pastille " + missingPastille.getPastille().getCode());
        evenement.setExecRonde(currentPastille.getExecRonde());
        evenement.setExecRondePastille(currentPastille);
        evenement.setSiteId(currentPastille.getExecRonde().getSite().getId());
        evenement.setRondeId(currentPastille.getExecRonde().getRefRonde().getId());
        evenement.setPastilleId(currentPastille.getPastille().getId());
        evenement.setDetectedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setCreatedBy(userService.getConnectedUserId());

        incidentRepository.save(evenement);
    }

    /**
     * Vérifier les retards
     */
    private void checkDelays(Exec_ronde_pastille execPastille) {
        if (execPastille.getIsLate() != null && execPastille.getIsLate() &&
                execPastille.getLateMinutes() != null && execPastille.getLateMinutes() > 5) {
            // Retard significatif
            createDelayIncident(execPastille);
        }
    }

    /**
     * Créer un incident de retard
     */
    private void createDelayIncident(Exec_ronde_pastille execPastille) {
        Evenement evenement = new Evenement();
        evenement.setType(IncidentType.RETARD);
        evenement.setSeverity(IncidentSeverity.LOW);
        evenement.setStatus(IncidentStatus.OPEN);
        evenement.setTitle("Retard détecté");
        evenement.setDescription("Retard de " + execPastille.getLateMinutes() + " minutes pour la pastille " +
                execPastille.getPastille().getCode());
        evenement.setExecRonde(execPastille.getExecRonde());
        evenement.setExecRondePastille(execPastille);
        evenement.setSiteId(execPastille.getExecRonde().getSite().getId());
        evenement.setRondeId(execPastille.getExecRonde().getRefRonde().getId());
        evenement.setPastilleId(execPastille.getPastille().getId());
        evenement.setDelayMinutes(execPastille.getLateMinutes());
        evenement.setExpectedTime(execPastille.getExpectedTime());
        evenement.setActualTime(execPastille.getActualTime());
        evenement.setDetectedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setCreatedBy(userService.getConnectedUserId());

        incidentRepository.save(evenement);
    }

    /**
     * Vérifier le temps de trajet
     */
    private void checkTravelTime(Exec_ronde_pastille execPastille, Exec_ronde execRonde) {
        List<Exec_ronde_pastille> allPastilles = execRondePastilleRepository
                .findByExecRondeIdOrderBySeqNo(execRonde.getId());

        // Trouver l'index de la pastille actuelle
        int currentIndex = -1;
        for (int i = 0; i < allPastilles.size(); i++) {
            if (allPastilles.get(i).getId().equals(execPastille.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            // Calculer le temps de trajet réel
            Exec_ronde_pastille previousPastille = allPastilles.get(currentIndex - 1);
            if (previousPastille.getActualTime() != null && execPastille.getActualTime() != null) {
                Duration actualTravel = Duration.between(previousPastille.getActualTime(), execPastille.getActualTime());
                execPastille.setActualTravelSec((int) actualTravel.getSeconds());

                // Vérifier si le temps de trajet est trop long
                if (execPastille.getExpectedTravelSec() != null &&
                        actualTravel.getSeconds() > execPastille.getExpectedTravelSec() * 1.5) {
                    createTravelTimeIncident(execPastille, previousPastille, actualTravel);
                }
            }
        }
    }

    /**
     * Créer un incident de temps de trajet trop long
     */
    private void createTravelTimeIncident(Exec_ronde_pastille execPastille, Exec_ronde_pastille previousPastille, Duration actualTravel) {
        Evenement evenement = new Evenement();
        evenement.setType(IncidentType.TEMPS_TRAJET_TROP_LONG);
        evenement.setSeverity(IncidentSeverity.MEDIUM);
        evenement.setStatus(IncidentStatus.OPEN);
        evenement.setTitle("Temps de trajet trop long");
        evenement.setDescription("Temps de trajet entre " + previousPastille.getPastille().getCode() +
                " et " + execPastille.getPastille().getCode() + " est de " +
                actualTravel.toMinutes() + " minutes (attendu: " +
                (execPastille.getExpectedTravelSec() / 60) + " minutes)");
        evenement.setExecRonde(execPastille.getExecRonde());
        evenement.setExecRondePastille(execPastille);
        evenement.setSiteId(execPastille.getExecRonde().getSite().getId());
        evenement.setRondeId(execPastille.getExecRonde().getRefRonde().getId());
        evenement.setPastilleId(execPastille.getPastille().getId());
        evenement.setDetectedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setCreatedBy(userService.getConnectedUserId());

        incidentRepository.save(evenement);
    }

    /**
     * Vérifier les retards globaux
     */
    private void checkGlobalDelays(Exec_ronde execRonde, List<Exec_ronde_pastille> execPastilles) {
        // Compter le nombre de pastilles en retard
        long lateCount = execPastilles.stream()
                .filter(p -> p.getIsLate() != null && p.getIsLate())
                .count();

        // Si plus de 50% des pastilles sont en retard, créer un incident global
        if (execPastilles.size() > 0 && lateCount > execPastilles.size() / 2) {
            createGlobalDelayIncident(execRonde, (int) lateCount, execPastilles.size());
        }
    }

    /**
     * Créer un incident de retard global
     */
    private void createGlobalDelayIncident(Exec_ronde execRonde, int lateCount, int totalCount) {
        Evenement evenement = new Evenement();
        evenement.setType(IncidentType.RETARD);
        evenement.setSeverity(IncidentSeverity.HIGH);
        evenement.setStatus(IncidentStatus.OPEN);
        evenement.setTitle("Retard global de la ronde");
        evenement.setDescription(lateCount + " pastilles sur " + totalCount + " sont en retard");
        evenement.setExecRonde(execRonde);
        evenement.setSiteId(execRonde.getSite().getId());
        evenement.setRondeId(execRonde.getRefRonde().getId());
        evenement.setDetectedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setCreatedBy(userService.getConnectedUserId());

        incidentRepository.save(evenement);
    }

    /**
     * Mettre à jour le taux de complétion
     */
    private void updateCompletionRate(Exec_ronde execRonde, List<Exec_ronde_pastille> execPastilles) {
        long doneCount = execPastilles.stream()
                .filter(p -> p.getStatus() == Status_ronde_pastille.DONE)
                .count();

        if (execPastilles.size() > 0) {
            BigDecimal completionRate = BigDecimal.valueOf(doneCount * 100.0 / execPastilles.size())
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            execRonde.setCompletionRate(completionRate);

            // Mettre à jour le statut si la ronde est terminée
            if (completionRate.compareTo(new BigDecimal("95")) >= 0) {
                execRonde.setStatus(Status_exec_Ronde.DONE);
                execRonde.setEndedAt(LocalDateTime.now());
            } else if (completionRate.compareTo(new BigDecimal("50")) >= 0) {
                execRonde.setStatus(Status_exec_Ronde.IN_PROGRESS);
            }

            execRonde.setUpdated_at(LocalDateTime.now());
            execRonde.setUpdated_by(userService.getConnectedUserId());
            execRondeRepository.save(execRonde);
        }
    }

    /**
     * Parser les IDs des rondes
     */
    private List<Long> parseRondeIds(String rondeIdsString) {
        if (rondeIdsString == null || rondeIdsString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(rondeIdsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * API pour exécuter un job manuellement
     */
    public SysJobRun executeJobManually(Long jobId) {
        return executeJob(jobId);
    }

    /**
     * Obtenir les statistiques d'exécution
     */
    public Map<String, Object> getExecutionStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Statistiques des job runs
        List<SysJobRun> jobRuns = sysJobRunRepository.findByDateRange(startDateTime, endDateTime);
        stats.put("totalJobRuns", jobRuns.size());
        stats.put("successfulJobRuns", jobRuns.stream()
                .filter(r -> r.getStatus() == JobRunStatus.OK)
                .count());
        stats.put("failedJobRuns", jobRuns.stream()
                .filter(r -> r.getStatus() == JobRunStatus.ERROR)
                .count());

        // Statistiques des rondes exécutées
        List<Exec_ronde> execRondes = execRondeRepository
                .findByPlannedStartAtBetween(startDateTime, endDateTime);
        stats.put("totalRondesExecuted", execRondes.size());

        // Statistiques des incidents
        List<Evenement> evenements = incidentRepository
                .findByDetectedAtBetween(startDateTime, endDateTime);
        stats.put("totalIncidents", evenements.size());

        // Répartition par type d'incident
        Map<IncidentType, Long> incidentByType = evenements.stream()
                .collect(Collectors.groupingBy(Evenement::getType, Collectors.counting()));
        stats.put("incidentsByType", incidentByType);

        return stats;
    }
}