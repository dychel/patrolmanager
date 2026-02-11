package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.*;
import com.patrolmanagr.patrolmanagr.dto.IncidentDTO;
import com.patrolmanagr.patrolmanagr.entity.*;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobExecutionService {

    @Autowired
    private SysJobRepository sysJobRepository;

    @Autowired
    private SysJobRunRepository sysJobRunRepository;

    @Autowired
    private RefRondeRepository refRondeRepository;

    @Autowired
    private ExecRondeRepository execRondeRepository;

    @Autowired
    private FactPointageRepository factPointageRepository;

    @Autowired
    private EvenementService evenementService;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private ExecRondePastilleRepository execRondePastilleRepository;

    @Autowired
    private RefRondePastilleRepository refRondePastilleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RefPastilleService refPastilleService;

    // Map des jobs par code
    private static final Map<String, String> JOB_NAME_TO_CODE = Map.of(
            "Génération des rondes exécutables", "JOB_GEN_RONDES",
            "Détection des rondes non effectuées", "JOB_DETECT_ANOM",
            "Synchronisation des référentiels", "JOB_SYNC_REF",
            "Calcul des statistiques journalières", "JOB_CALC_STATS",
            "Purge des logs anciens", "JOB_PURGE_LOGS",
            "Vérification des terminaux", "JOB_CHECK_TERMINALS"
    );

    // Job principal exécuté toutes les minutes
    @Scheduled(cron = "0 * * * * *")
    public void executeScheduledJobs() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        List<SysJob> activeJobs = sysJobRepository.findByIsEnabledTrue();

        for (SysJob job : activeJobs) {
            try {
                if (shouldExecuteJob(job, now, currentTime)) {
                    executeJob(job);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'exécution du job " + job.getJobCode() + ": " + e.getMessage());
                createJobErrorEvent(job, e.getMessage());
            }
        }
    }

    private boolean shouldExecuteJob(SysJob job, LocalDateTime now, LocalTime currentTime) {
        switch (job.getScheduleTypeJob()) {
            case HOURLY:
                if (job.getScheduleIntervalMin() != null) {
                    SysJobRun lastRun = getLastJobRun(job.getId());
                    if (lastRun == null) {
                        return true;
                    }
                    Duration sinceLastRun = Duration.between(lastRun.getStartedAt(), now);
                    return sinceLastRun.toMinutes() >= job.getScheduleIntervalMin();
                }
                return false;

            case DAILY:
                if (job.getScheduleTime() != null) {
                    return currentTime.getHour() == job.getScheduleTime().getHour() &&
                            currentTime.getMinute() == job.getScheduleTime().getMinute();
                }
                return false;

            case WEEKLY:
                if (job.getScheduleTime() != null) {
                    return now.getDayOfWeek().getValue() == 1 &&
                            currentTime.getHour() == job.getScheduleTime().getHour() &&
                            currentTime.getMinute() == job.getScheduleTime().getMinute();
                }
                return false;

            case MANUEL:
                return false;

            default:
                return false;
        }
    }

    public SysJobRun executeJob(SysJob job) {
        SysJobRun jobRun = startJobRun(job);

        try {
            int totalRondes = 0;
            int totalPointages = 0;
            int totalEvenements = 0;
            int totalIncidents = 0;

            switch (job.getJobCode()) {
                case "JOB_GEN_RONDES":
                    totalRondes = generateExecutableRondes(job, jobRun);
                    break;

                case "JOB_DETECT_ANOM":
                    Map<String, Integer> results = detectAnomalies(job, jobRun);
                    totalRondes = results.get("rondes");
                    totalEvenements = results.get("evenements");
                    totalIncidents = results.get("incidents");
                    break;

                case "JOB_SYNC_REF":
                    syncReferenceData(jobRun);
                    break;

                case "JOB_CALC_STATS":
                    calculateDailyStats(jobRun);
                    break;

                case "JOB_PURGE_LOGS":
                    purgeOldLogs(jobRun);
                    break;

                case "JOB_CHECK_TERMINALS":
                    checkTerminals(jobRun);
                    break;

                default:
                    throw new ApiRequestException("Type de job non supporté: " + job.getJobCode());
            }

            updateJobRunStats(jobRun, totalRondes, totalPointages, totalEvenements, totalIncidents);

            return completeJobRun(jobRun, JobRunStatus.OK,
                    String.format("Exécution terminée: %d rondes, %d événements, %d incidents",
                            totalRondes, totalEvenements, totalIncidents));

        } catch (Exception e) {
            return completeJobRun(jobRun, JobRunStatus.ERROR,
                    "Erreur lors de l'exécution: " + e.getMessage());
        }
    }

    private int generateExecutableRondes(SysJob job, SysJobRun jobRun) {
        LocalDate today = LocalDate.now();
        int generatedCount = 0;

        List<Ref_ronde> allRondes = refRondeRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == Status.ACTIVE)
                .collect(Collectors.toList());

        for (Ref_ronde ronde : allRondes) {
            try {
                if (shouldExecuteRondeToday(ronde, today)) {
                    List<Exec_ronde> existing = execRondeRepository
                            .findByRondeIdAndDate(ronde.getId(), today);

                    if (existing.isEmpty()) {
                        Exec_ronde execRonde = createExecRondeFromRefRonde(ronde, jobRun);
                        generatedCount++;

                        evenementService.createPositiveEvent(
                                "Ronde générée",
                                "Ronde " + ronde.getCode() + " générée pour exécution",
                                EvenementType.RONDE_GENEREE,
                                execRonde.getId(),
                                null
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur génération ronde " + ronde.getId() + ": " + e.getMessage());
            }
        }

        evenementService.createPositiveEvent(
                "Job exécuté: Génération des rondes",
                generatedCount + " rondes générées",
                EvenementType.JOB_EXECUTE,
                null,
                null
        );

        return generatedCount;
    }

    private Map<String, Integer> detectAnomalies(SysJob job, SysJobRun jobRun) {
        LocalDate today = LocalDate.now();
        int rondesAnalyzed = 0;
        int evenementsCreated = 0;
        int incidentsCreated = 0;

        List<Exec_ronde> todayExecRondes = execRondeRepository.findByExecDate(today);

        for (Exec_ronde execRonde : todayExecRondes) {
            try {
                rondesAnalyzed++;

                analyzePointagesForRonde(execRonde, jobRun);

                evenementsCreated += detectMissingPastilles(execRonde);
                evenementsCreated += checkDelays(execRonde);
                evenementsCreated += checkSequence(execRonde);
                evenementsCreated += checkRondeIncomplete(execRonde);

                incidentsCreated += checkCriticalIssues(execRonde);

            } catch (Exception e) {
                System.err.println("Erreur analyse ronde " + execRonde.getId() + ": " + e.getMessage());
            }
        }

        Map<String, Integer> results = new HashMap<>();
        results.put("rondes", rondesAnalyzed);
        results.put("evenements", evenementsCreated);
        results.put("incidents", incidentsCreated);

        return results;
    }

    private void analyzePointagesForRonde(Exec_ronde execRonde, SysJobRun jobRun) {
        LocalDate execDate = execRonde.getExecDate();
        LocalDateTime startOfDay = execDate.atStartOfDay();
        LocalDateTime endOfDay = execDate.atTime(LocalTime.MAX);

        List<Fact_pointage> pointages = factPointageRepository
                .findBySiteIdAndEventTimeBetween(
                        execRonde.getSite().getId(),
                        startOfDay,
                        endOfDay
                );

        pointages = pointages.stream()
                .filter(p -> p.getRondeId() != null && p.getRondeId().equals(execRonde.getRefRonde().getId()))
                .collect(Collectors.toList());

        for (Fact_pointage pointage : pointages) {
            associatePointageWithPastille(pointage, execRonde, jobRun);
        }
    }

    private void associatePointageWithPastille(Fact_pointage pointage, Exec_ronde execRonde, SysJobRun jobRun) {
        if (pointage.getPastilleCodeRaw() == null) {
            return;
        }

        try {
            Ref_pastille pastille = refPastilleService.findPastilleByExternalUid(pointage.getPastilleCodeRaw());

            if (pastille == null) {
                evenementService.createAlertEvent(
                        "Pastille inconnue",
                        "Pastille avec external_uid " + pointage.getPastilleCodeRaw() + " non trouvée",
                        EvenementType.HORS_PLAQUETTE,
                        EvenementSeverity.MOYENNE,
                        execRonde.getId(),
                        null
                );
                return;
            }

            List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                    .findByExecRondeIdAndPastilleId(execRonde.getId(), pastille.getId());

            if (!execPastilles.isEmpty()) {
                Exec_ronde_pastille execPastille = execPastilles.get(0);

                if (execPastille.getStatus() == Status_ronde_pastille.DONE) {
                    evenementService.createAlertEvent(
                            "Double scan détecté",
                            "Pastille " + execPastille.getPastille().getCode() + " scannée plusieurs fois",
                            EvenementType.DOUBLE_SCAN,
                            EvenementSeverity.FAIBLE,
                            execRonde.getId(),
                            execPastille.getId()
                    );
                    return;
                }

                updateExecPastilleFromPointage(execPastille, pointage);

                evenementService.createPositiveEvent(
                        "Pastille scannée",
                        "Pastille " + execPastille.getPastille().getCode() + " scannée correctement",
                        EvenementType.PASTILLE_SCANNEE,
                        execRonde.getId(),
                        execPastille.getId()
                );

            } else {
                evenementService.createAlertEvent(
                        "Pastille non attendue",
                        "Pastille " + pastille.getCode() + " scannée hors de la ronde prévue",
                        EvenementType.HORS_PLAQUETTE,
                        EvenementSeverity.MOYENNE,
                        execRonde.getId(),
                        null
                );
            }

        } catch (ApiRequestException e) {
            evenementService.createAlertEvent(
                    "Pastille inconnue",
                    "Pastille avec external_uid " + pointage.getPastilleCodeRaw() + " non enregistrée",
                    EvenementType.HORS_PLAQUETTE,
                    EvenementSeverity.MOYENNE,
                    execRonde.getId(),
                    null
            );
        }
    }

    private int detectMissingPastilles(Exec_ronde execRonde) {
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeId(execRonde.getId());

        int missingCount = 0;
        for (Exec_ronde_pastille execPastille : execPastilles) {
            if (execPastille.getStatus() == Status_ronde_pastille.EXPECTED ||
                    execPastille.getStatus() == Status_ronde_pastille.MISSED) {

                long delayMinutes = 0;
                if (execPastille.getExpectedTime() != null) {
                    delayMinutes = Duration.between(
                            execPastille.getExpectedTime(),
                            LocalDateTime.now()
                    ).toMinutes();
                }

                EvenementSeverity severity = EvenementSeverity.FAIBLE;
                if (delayMinutes > 30) {
                    severity = EvenementSeverity.ELEVEE;
                } else if (delayMinutes > 15) {
                    severity = EvenementSeverity.MOYENNE;
                }

                EvenementType type = EvenementType.PASTILLE_MANQUANTE;
                if (delayMinutes > 30) {
                    type = EvenementType.OMISSION_PASTILLE;
                }

                evenementService.createAlertEvent(
                        type == EvenementType.OMISSION_PASTILLE ? "Omission pastille" : "Pastille manquante",
                        "Pastille " + execPastille.getPastille().getCode() + " non scannée (retard: " + delayMinutes + " min)",
                        type,
                        severity,
                        execRonde.getId(),
                        execPastille.getId()
                );

                missingCount++;
            }
        }

        return missingCount;
    }

    private int checkDelays(Exec_ronde execRonde) {
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeId(execRonde.getId());

        int delayEvents = 0;
        for (Exec_ronde_pastille execPastille : execPastilles) {
            if (execPastille.getIsLate() != null && execPastille.getIsLate() &&
                    execPastille.getLateMinutes() != null && execPastille.getLateMinutes() > 0) {

                EvenementSeverity severity = EvenementSeverity.FAIBLE;
                EvenementType type = EvenementType.RETARD_MODERE;

                if (execPastille.getLateMinutes() > 30) {
                    severity = EvenementSeverity.ELEVEE;
                    type = EvenementType.RETARD_IMPORTANT;
                } else if (execPastille.getLateMinutes() > 15) {
                    severity = EvenementSeverity.MOYENNE;
                    type = EvenementType.RETARD_MODERE;
                } else if (execPastille.getLateMinutes() > 5) {
                    severity = EvenementSeverity.FAIBLE;
                    type = EvenementType.RETARD_MODERE;
                }

                evenementService.createAlertEvent(
                        type == EvenementType.RETARD_IMPORTANT ? "Retard important" : "Retard modéré",
                        "Retard de " + execPastille.getLateMinutes() + " minutes pour la pastille " +
                                execPastille.getPastille().getCode(),
                        type,
                        severity,
                        execRonde.getId(),
                        execPastille.getId()
                );

                delayEvents++;
            }
        }

        return delayEvents;
    }

    private int checkSequence(Exec_ronde execRonde) {
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeIdOrderBySeqNo(execRonde.getId());

        int sequenceErrors = 0;
        for (int i = 0; i < execPastilles.size(); i++) {
            Exec_ronde_pastille current = execPastilles.get(i);

            if (current.getStatus() == Status_ronde_pastille.DONE && i > 0) {
                Exec_ronde_pastille previous = execPastilles.get(i - 1);
                if (previous.getStatus() != Status_ronde_pastille.DONE) {
                    evenementService.createAlertEvent(
                            "Erreur de séquence",
                            "Pastille " + current.getPastille().getCode() +
                                    " scannée avant la pastille " + previous.getPastille().getCode(),
                            EvenementType.SEQUENCE_INCORRECTE,
                            EvenementSeverity.MOYENNE,
                            execRonde.getId(),
                            current.getId()
                    );
                    sequenceErrors++;
                }
            }
        }

        return sequenceErrors;
    }

    private int checkRondeIncomplete(Exec_ronde execRonde) {
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeId(execRonde.getId());

        long totalPastilles = execPastilles.size();
        long scannedPastilles = execPastilles.stream()
                .filter(p -> p.getStatus() == Status_ronde_pastille.DONE)
                .count();

        long missingPastilles = totalPastilles - scannedPastilles;

        if (missingPastilles > 0 && scannedPastilles < totalPastilles) {
            EvenementSeverity severity = EvenementSeverity.FAIBLE;

            if (missingPastilles > totalPastilles / 2) {
                severity = EvenementSeverity.ELEVEE;
            } else if (missingPastilles > totalPastilles / 4) {
                severity = EvenementSeverity.MOYENNE;
            }

            evenementService.createAlertEvent(
                    "Ronde incomplète",
                    missingPastilles + " pastilles sur " + totalPastilles + " non scannées",
                    EvenementType.RONDE_INCOMPLETE,
                    severity,
                    execRonde.getId(),
                    null
            );

            return 1;
        }

        return 0;
    }

    private int checkCriticalIssues(Exec_ronde execRonde) {
        List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                .findByExecRondeId(execRonde.getId());

        long totalPastilles = execPastilles.size();
        long missingPastilles = execPastilles.stream()
                .filter(p -> p.getStatus() == Status_ronde_pastille.EXPECTED ||
                        p.getStatus() == Status_ronde_pastille.MISSED)
                .count();

        int incidentsCreated = 0;

        // Incident: plus de 50% de pastilles manquantes
        if (totalPastilles > 0 && missingPastilles > totalPastilles / 2) {
            IncidentDTO incidentDTO = new IncidentDTO();
            incidentDTO.setTitle("Ronde gravement incomplète");
            incidentDTO.setDescription(missingPastilles + " pastilles sur " + totalPastilles + " manquantes");
            incidentDTO.setType(IncidentType.PASTILLE_MANQUANTE);
            incidentDTO.setSeverity(IncidentSeverity.ELEVEE);
            incidentDTO.setStatus(IncidentStatus.OUVERT);
            incidentDTO.setExecRondeId(execRonde.getId());
            incidentDTO.setSiteId(execRonde.getSite().getId());
            incidentDTO.setRondeId(execRonde.getRefRonde().getId());

            incidentService.createIncident(incidentDTO);
            incidentsCreated++;
        }

        // Incident: Retard important sur plusieurs pastilles (>30 min sur au moins 3 pastilles)
        long importantDelays = execPastilles.stream()
                .filter(p -> p.getLateMinutes() != null && p.getLateMinutes() > 30)
                .count();

        if (importantDelays >= 3) {
            IncidentDTO incidentDTO = new IncidentDTO();
            incidentDTO.setTitle("Retards importants multiples");
            incidentDTO.setDescription(importantDelays + " pastilles avec un retard supérieur à 30 minutes");
            incidentDTO.setType(IncidentType.RETARD);
            incidentDTO.setSeverity(IncidentSeverity.MOYENNE);
            incidentDTO.setStatus(IncidentStatus.OUVERT);
            incidentDTO.setExecRondeId(execRonde.getId());
            incidentDTO.setSiteId(execRonde.getSite().getId());
            incidentDTO.setRondeId(execRonde.getRefRonde().getId());

            incidentService.createIncident(incidentDTO);
            incidentsCreated++;
        }

        // Incident: Double scan répété
        long doubleScans = evenementService.countEvenementsByExecRondeAndType(
                execRonde.getId(), EvenementType.DOUBLE_SCAN);

        if (doubleScans >= 3) {
            IncidentDTO incidentDTO = new IncidentDTO();
            incidentDTO.setTitle("Multiples doubles scans");
            incidentDTO.setDescription(doubleScans + " doubles scans détectés sur cette ronde");
            incidentDTO.setType(IncidentType.DOUBLE_SCAN);
            incidentDTO.setSeverity(IncidentSeverity.MOYENNE);
            incidentDTO.setStatus(IncidentStatus.OUVERT);
            incidentDTO.setExecRondeId(execRonde.getId());
            incidentDTO.setSiteId(execRonde.getSite().getId());
            incidentDTO.setRondeId(execRonde.getRefRonde().getId());

            incidentService.createIncident(incidentDTO);
            incidentsCreated++;
        }

        return incidentsCreated;
    }

    private boolean shouldExecuteRondeToday(Ref_ronde ronde, LocalDate today) {
        if (ronde.getJoursSemaine() == null || ronde.getJoursSemaine().isEmpty()) {
            return true;
        }

        String[] jours = ronde.getJoursSemaine().split(",");
        String todayCode = getDayCode(today.getDayOfWeek().getValue());

        for (String jour : jours) {
            if (jour.trim().equalsIgnoreCase(todayCode)) {
                return true;
            }
        }

        return false;
    }

    private String getDayCode(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "L";
            case 2: return "MA";
            case 3: return "ME";
            case 4: return "J";
            case 5: return "V";
            case 6: return "S";
            case 7: return "D";
            default: return "";
        }
    }

    private Exec_ronde createExecRondeFromRefRonde(Ref_ronde ronde, SysJobRun jobRun) {
        Exec_ronde execRonde = new Exec_ronde();
        execRonde.setRefRonde(ronde);
        execRonde.setSite(ronde.getRef_site());
        execRonde.setExecDate(LocalDate.now());
        execRonde.setStatus(Status_exec_Ronde.PLANNED);
        execRonde.setJobRun(jobRun);

        LocalDateTime plannedStart = calculatePlannedStart(ronde);
        LocalDateTime plannedEnd = calculatePlannedEnd(ronde, plannedStart);

        execRonde.setPlannedStartAt(plannedStart);
        execRonde.setPlannedEndAt(plannedEnd);
        execRonde.setCreated_at(LocalDateTime.now());
        execRonde.setCreated_by(userService.getConnectedUserId());

        execRondeRepository.save(execRonde);

        createExecRondePastilles(execRonde);

        return execRonde;
    }

    private void createExecRondePastilles(Exec_ronde execRonde) {
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

            if (refPastille.getExpected_travel_sec() != null) {
                currentExpectedTime = currentExpectedTime.plusSeconds(refPastille.getExpected_travel_sec());
            } else {
                currentExpectedTime = currentExpectedTime.plusMinutes(5);
            }

            execRondePastilleRepository.save(execPastille);
        }
    }

    private void updateExecPastilleFromPointage(Exec_ronde_pastille execPastille, Fact_pointage pointage) {
        execPastille.setStatus(Status_ronde_pastille.DONE);
        execPastille.setScannedAt(pointage.getEventTime());
        execPastille.setActualTime(pointage.getEventTime());
        execPastille.setPointageId(pointage.getId());
        execPastille.setUpdated_at(LocalDateTime.now());
        execPastille.setUpdated_by(userService.getConnectedUserId());

        if (execPastille.getExpectedTime() != null) {
            Duration deviation = Duration.between(execPastille.getExpectedTime(), pointage.getEventTime());
            execPastille.setDeviationSec((int) deviation.getSeconds());
            execPastille.setIsLate(deviation.getSeconds() > 0);
            execPastille.setLateMinutes((int) deviation.toMinutes());
        }

        execRondePastilleRepository.save(execPastille);
    }

    private LocalDateTime calculatePlannedStart(Ref_ronde ronde) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime heureDebut = ronde.getHeure_debut();

        if (heureDebut != null) {
            return LocalDateTime.of(now.toLocalDate(), heureDebut);
        }

        return now;
    }

    private LocalDateTime calculatePlannedEnd(Ref_ronde ronde, LocalDateTime plannedStart) {
        Integer dureeMinutes = ronde.getExpected_duration_min();

        if (dureeMinutes != null && dureeMinutes > 0) {
            return plannedStart.plusMinutes(dureeMinutes);
        }

        return plannedStart.plusHours(1);
    }

    private SysJobRun startJobRun(SysJob job) {
        SysJobRun jobRun = new SysJobRun();
        jobRun.setJob(job);
        jobRun.setStartedAt(LocalDateTime.now());
        jobRun.setStatus(JobRunStatus.RUNNING);
        jobRun.setMessage("Démarrage de l'exécution");

        return sysJobRunRepository.save(jobRun);
    }

    private SysJobRun completeJobRun(SysJobRun jobRun, JobRunStatus status, String message) {
        jobRun.setEndedAt(LocalDateTime.now());
        jobRun.setStatus(status);
        jobRun.setMessage(message);

        if (jobRun.getStartedAt() != null && jobRun.getEndedAt() != null) {
            Duration duration = Duration.between(jobRun.getStartedAt(), jobRun.getEndedAt());
            jobRun.setDurationMs(duration.toMillis());
        }

        return sysJobRunRepository.save(jobRun);
    }

    private void updateJobRunStats(SysJobRun jobRun, int rondeCount, int pointageCount,
                                   int evenementCount, int incidentCount) {
        jobRun.setRondeCount(rondeCount);
        jobRun.setPointageCount(pointageCount);
        jobRun.setIncidentCount(incidentCount);
        sysJobRunRepository.save(jobRun);
    }

    private SysJobRun getLastJobRun(Long jobId) {
        return sysJobRunRepository.findTopByJobIdOrderByStartedAtDesc(jobId)
                .orElse(null);
    }

    private void createJobErrorEvent(SysJob job, String errorMessage) {
        evenementService.createAlertEvent(
                "Erreur d'exécution de job",
                "Job " + job.getName() + " a échoué: " + errorMessage,
                EvenementType.JOB_EXECUTE,
                EvenementSeverity.MOYENNE,
                null,
                null
        );
    }

    private void syncReferenceData(SysJobRun jobRun) {
        evenementService.createPositiveEvent(
                "Synchronisation terminée",
                "Synchronisation des référentiels effectuée",
                EvenementType.SYNCHRONISATION_TERMINEE,
                null,
                null
        );
    }

    private void calculateDailyStats(SysJobRun jobRun) {
        evenementService.createPositiveEvent(
                "Statistiques calculées",
                "Calcul des statistiques journalières effectué",
                EvenementType.JOB_EXECUTE,
                null,
                null
        );
    }

    private void purgeOldLogs(SysJobRun jobRun) {
        evenementService.createPositiveEvent(
                "Logs purgés",
                "Purge des logs anciens effectuée",
                EvenementType.JOB_EXECUTE,
                null,
                null
        );
    }

    private void checkTerminals(SysJobRun jobRun) {
        evenementService.createPositiveEvent(
                "Terminaux vérifiés",
                "Vérification des terminaux effectuée",
                EvenementType.JOB_EXECUTE,
                null,
                null
        );

        // Simulation: création d'un événement terminal inactif
        evenementService.createAlertEvent(
                "Terminal inactif",
                "Certains terminaux n'ont pas envoyé de données depuis plus de 24h",
                EvenementType.TERMINAL_INACTIF,
                EvenementSeverity.MOYENNE,
                null,
                null
        );
    }

    public SysJobRun executeJobManually(Long jobId) {
        SysJob job = sysJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        if (job.getScheduleTypeJob() != ScheduleTypeJob.MANUEL) {
            throw new ApiRequestException("Ce job ne peut être exécuté manuellement");
        }

        return executeJob(job);
    }

    public SysJobRun forceExecuteJob(Long jobId) {
        SysJob job = sysJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        return executeJob(job);
    }
}