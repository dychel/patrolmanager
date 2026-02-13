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

    // Map des jobs par code - AJOUT DU NOUVEAU JOB
    private static final Map<String, String> JOB_NAME_TO_CODE = Map.of(
            "Génération des rondes exécutables", "JOB_GEN_RONDES",
            "Détection des rondes non effectuées", "JOB_DETECT_ANOM",
            "Synchronisation des référentiels", "JOB_SYNC_REF",
            "Calcul des statistiques journalières", "JOB_CALC_STATS",
            "Purge des logs anciens", "JOB_PURGE_LOGS",
            "Vérification des terminaux", "JOB_CHECK_TERMINALS",
            "Intégration des pointages dans les rondes", "JOB_INTEGRATION_POINTAGES"
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

                case "JOB_INTEGRATION_POINTAGES":
                    Map<String, Integer> integrationResults = integrerPointagesDansRondes(job, jobRun);
                    totalRondes = integrationResults.get("rondes");
                    totalPointages = integrationResults.get("pointages");
                    totalEvenements = integrationResults.get("evenements");
                    totalIncidents = integrationResults.get("incidents");
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
                    String.format("Exécution terminée: %d rondes, %d pointages, %d événements, %d incidents",
                            totalRondes, totalPointages, totalEvenements, totalIncidents));

        } catch (Exception e) {
            return completeJobRun(jobRun, JobRunStatus.ERROR,
                    "Erreur lors de l'exécution: " + e.getMessage());
        }
    }

    /**
     * NOUVEAU JOB: Intégration des pointages dans les rondes
     * Associe les pointages (fact_pointage) aux pastilles des rondes exécutables (exec_ronde_pastille)
     * en utilisant l'external_uid comme clé de correspondance
     */
    private Map<String, Integer> integrerPointagesDansRondes(SysJob job, SysJobRun jobRun) {
        LocalDate today = LocalDate.now();
        int rondesTraitees = 0;
        int pointagesIntegres = 0;
        int evenementsCrees = 0;
        int incidentsCrees = 0;

        // Récupérer toutes les rondes exécutables du jour
        List<Exec_ronde> rondesDuJour = execRondeRepository.findByExecDate(today);

        for (Exec_ronde execRonde : rondesDuJour) {
            try {
                rondesTraitees++;

                // Récupérer les pointages du site pour aujourd'hui
                LocalDateTime startOfDay = today.atStartOfDay();
                LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

                List<Fact_pointage> pointagesSite = factPointageRepository
                        .findBySiteIdAndEventTimeBetween(
                                execRonde.getSite().getId(),
                                startOfDay,
                                endOfDay
                        );

                // Traiter les pointages pour cette ronde
                Map<String, Integer> resultatsRonde = traiterPointagesPourRonde(pointagesSite, execRonde, jobRun);
                pointagesIntegres += resultatsRonde.get("pointages");
                evenementsCrees += resultatsRonde.get("evenements");
                incidentsCrees += resultatsRonde.get("incidents");

            } catch (Exception e) {
                System.err.println("Erreur lors de l'intégration pour la ronde " + execRonde.getId() + ": " + e.getMessage());
                evenementService.createAlertEvent(
                        "Erreur d'intégration",
                        "Erreur lors de l'intégration des pointages pour la ronde " + execRonde.getRefRonde().getCode(),
                        EvenementType.AUTRE,
                        EvenementSeverity.MOYENNE,
                        execRonde.getId(),
                        null
                );
                evenementsCrees++;
            }
        }

        // Événement récapitulatif
        evenementService.createPositiveEvent(
                "Job exécuté: Intégration des pointages",
                String.format("%d rondes traitées, %d pointages intégrés, %d événements",
                        rondesTraitees, pointagesIntegres, evenementsCrees),
                EvenementType.JOB_EXECUTE,
                null,
                null
        );

        Map<String, Integer> results = new HashMap<>();
        results.put("rondes", rondesTraitees);
        results.put("pointages", pointagesIntegres);
        results.put("evenements", evenementsCrees);
        results.put("incidents", incidentsCrees);

        return results;
    }

    /**
     * Traite tous les pointages pour une ronde spécifique
     */
    private Map<String, Integer> traiterPointagesPourRonde(List<Fact_pointage> pointagesSite, Exec_ronde execRonde, SysJobRun jobRun) {
        int pointagesIntegres = 0;
        int evenementsCrees = 0;
        int incidentsCrees = 0;

        // Grouper les pointages par pastille pour éviter les doublons
        Map<String, List<Fact_pointage>> pointagesParPastille = pointagesSite.stream()
                .filter(p -> p.getPastilleCodeRaw() != null && !p.getPastilleCodeRaw().isEmpty())
                .collect(Collectors.groupingBy(Fact_pointage::getPastilleCodeRaw));

        // Ensemble des pastilles scannées (pour détecter les manquantes)
        Set<String> externalUidsScannees = new HashSet<>();

        for (Map.Entry<String, List<Fact_pointage>> entry : pointagesParPastille.entrySet()) {
            String externalUid = entry.getKey();
            List<Fact_pointage> pointages = entry.getValue();

            // Prendre le pointage le plus récent
            Fact_pointage pointage = pointages.stream()
                    .max(Comparator.comparing(Fact_pointage::getEventTime))
                    .orElse(pointages.get(0));

            // Intégrer le pointage
            boolean integre = integrerPointageDansRonde(pointage, execRonde, jobRun);
            if (integre) {
                pointagesIntegres++;
                externalUidsScannees.add(externalUid);

                // Vérifier les doublons
                if (pointages.size() > 1) {
                    evenementService.createAlertEvent(
                            "Double scan détecté",
                            "Pastille avec code " + externalUid + " scannée " + pointages.size() + " fois",
                            EvenementType.DOUBLE_SCAN,
                            EvenementSeverity.FAIBLE,
                            execRonde.getId(),
                            null
                    );
                    evenementsCrees++;
                }
            }
        }

        // Vérifier les pastilles manquantes
        int pastillesManquantes = verifierPastillesManquantes(execRonde, externalUidsScannees);
        if (pastillesManquantes > 0) {
            evenementsCrees += pastillesManquantes;

            // Créer un incident si trop de pastilles manquantes
            if (pastillesManquantes > 3) {
                IncidentDTO incidentDTO = new IncidentDTO();
                incidentDTO.setTitle("Pastilles manquantes multiples");
                incidentDTO.setDescription(pastillesManquantes + " pastilles non pointées pour la ronde");
                incidentDTO.setType(IncidentType.PASTILLE_MANQUANTE);
                incidentDTO.setSeverity(IncidentSeverity.MOYENNE);
                incidentDTO.setStatus(IncidentStatus.OUVERT);
                incidentDTO.setExecRondeId(execRonde.getId());
                incidentDTO.setSiteId(execRonde.getSite().getId());
                incidentDTO.setRondeId(execRonde.getRefRonde().getId());

                incidentService.createIncident(incidentDTO);
                incidentsCrees++;
            }
        }

        // Vérifier la séquence
        int erreursSequence = verifierSequence(execRonde);
        if (erreursSequence > 0) {
            evenementsCrees += erreursSequence;
        }

        Map<String, Integer> results = new HashMap<>();
        results.put("pointages", pointagesIntegres);
        results.put("evenements", evenementsCrees);
        results.put("incidents", incidentsCrees);

        return results;
    }

    /**
     * Intègre un pointage dans une ronde exécutable
     */
    private boolean integrerPointageDansRonde(Fact_pointage pointage, Exec_ronde execRonde, SysJobRun jobRun) {
        if (pointage.getPastilleCodeRaw() == null || pointage.getPastilleCodeRaw().trim().isEmpty()) {
            return false;
        }

        try {
            // 1. Trouver la pastille référentielle par son external_uid
            Ref_pastille pastille = refPastilleService.findPastilleByExternalUid(pointage.getPastilleCodeRaw());

            if (pastille == null) {
                // Pastille inconnue dans le référentiel
                evenementService.createAlertEvent(
                        "Pastille inconnue",
                        "Pastille avec code " + pointage.getPastilleCodeRaw() + " non trouvée",
                        EvenementType.HORS_PLAQUETTE,
                        EvenementSeverity.MOYENNE,
                        execRonde.getId(),
                        null
                );
                return false;
            }

            // 2. Vérifier si cette pastille est dans la ronde
            List<Ref_ronde_pastille> refPastilles = refRondePastilleRepository
                    .findByRondeIdAndPastilleId(execRonde.getRefRonde().getId(), pastille.getId());

            if (refPastilles.isEmpty()) {
                // Pastille non prévue dans cette ronde
                evenementService.createAlertEvent(
                        "Pastille hors ronde",
                        "Pastille " + pastille.getCode() + " non prévue dans la ronde",
                        EvenementType.HORS_PLAQUETTE,
                        EvenementSeverity.MOYENNE,
                        execRonde.getId(),
                        null
                );
                return false;
            }

            // 3. Chercher si cette pastille est déjà intégrée à la ronde exécutable
            List<Exec_ronde_pastille> execPastilles = execRondePastilleRepository
                    .findByExecRondeIdAndPastilleId(execRonde.getId(), pastille.getId());

            Exec_ronde_pastille execPastille;
            if (execPastilles.isEmpty()) {
                // Créer une nouvelle entrée (cas rare où la pastille a été ajoutée après génération)
                execPastille = new Exec_ronde_pastille();
                execPastille.setExecRonde(execRonde);
                execPastille.setPastille(pastille);
                execPastille.setSeqNo(refPastilles.get(0).getSeq_no());
                execPastille.setExpectedTravelSec(refPastilles.get(0).getExpected_travel_sec());
                execPastille.setStatus(Status_ronde_pastille.EXPECTED);
                execPastille.setCreated_at(LocalDateTime.now());
                execPastille.setCreated_by(userService.getConnectedUserId());
            } else {
                execPastille = execPastilles.get(0);
            }

            // 4. Vérifier si déjà scannée
            if (execPastille.getStatus() == Status_ronde_pastille.DONE) {
                evenementService.createAlertEvent(
                        "Double scan",
                        "Pastille " + pastille.getCode() + " déjà scannée",
                        EvenementType.DOUBLE_SCAN,
                        EvenementSeverity.FAIBLE,
                        execRonde.getId(),
                        execPastille.getId()
                );
                return false;
            }

            // 5. Mettre à jour la pastille exécutable
            execPastille.setStatus(Status_ronde_pastille.DONE);
            execPastille.setScannedAt(pointage.getEventTime());
            execPastille.setActualTime(pointage.getEventTime());
            execPastille.setPointageId(pointage.getId());
            execPastille.setUpdated_at(LocalDateTime.now());
            execPastille.setUpdated_by(userService.getConnectedUserId());

            // Calculer l'écart par rapport à l'heure prévue
            if (execPastille.getExpectedTime() != null) {
                Duration deviation = Duration.between(execPastille.getExpectedTime(), pointage.getEventTime());
                execPastille.setDeviationSec((int) deviation.getSeconds());
                execPastille.setIsLate(deviation.getSeconds() > 0);
                execPastille.setLateMinutes((int) deviation.toMinutes());

                // Vérifier les retards
                if (deviation.toMinutes() > 5) {
                    EvenementType typeRetard = deviation.toMinutes() > 30 ?
                            EvenementType.RETARD_IMPORTANT : EvenementType.RETARD_MODERE;

                    EvenementSeverity severity = deviation.toMinutes() > 30 ?
                            EvenementSeverity.ELEVEE : EvenementSeverity.MOYENNE;

                    evenementService.createAlertEvent(
                            typeRetard == EvenementType.RETARD_IMPORTANT ? "Retard important" : "Retard modéré",
                            "Retard de " + deviation.toMinutes() + " minutes",
                            typeRetard,
                            severity,
                            execRonde.getId(),
                            execPastille.getId()
                    );
                }
            }

            execRondePastilleRepository.save(execPastille);

            // 6. Événement de succès
            evenementService.createPositiveEvent(
                    "Pastille scannée",
                    "Pastille " + pastille.getCode() + " intégrée",
                    EvenementType.PASTILLE_SCANNEE,
                    execRonde.getId(),
                    execPastille.getId()
            );

            return true;

        } catch (ApiRequestException e) {
            // Pastille non trouvée
            evenementService.createAlertEvent(
                    "Pastille inconnue",
                    "Code pastille " + pointage.getPastilleCodeRaw() + " non enregistré",
                    EvenementType.HORS_PLAQUETTE,
                    EvenementSeverity.MOYENNE,
                    execRonde.getId(),
                    null
            );
            return false;
        }
    }

    /**
     * Vérifie les pastilles manquantes dans une ronde
     */
    private int verifierPastillesManquantes(Exec_ronde execRonde, Set<String> externalUidsScannees) {
        List<Exec_ronde_pastille> pastillesAttendues = execRondePastilleRepository
                .findByExecRondeId(execRonde.getId());

        int manquantes = 0;
        for (Exec_ronde_pastille pastilleAttendue : pastillesAttendues) {
            if (pastilleAttendue.getStatus() != Status_ronde_pastille.DONE) {
                String externalUid = pastilleAttendue.getPastille().getExternal_uid();

                // Si la pastille n'a pas été scannée
                if (externalUid == null || !externalUidsScannees.contains(externalUid)) {

                    // Déterminer le type d'événement selon le délai
                    EvenementType type = EvenementType.PASTILLE_MANQUANTE;
                    EvenementSeverity severity = EvenementSeverity.MOYENNE;

                    if (pastilleAttendue.getExpectedTime() != null) {
                        long minutesRetard = Duration.between(
                                pastilleAttendue.getExpectedTime(),
                                LocalDateTime.now()
                        ).toMinutes();

                        if (minutesRetard > 60) {
                            type = EvenementType.OMISSION_PASTILLE;
                            severity = EvenementSeverity.ELEVEE;
                        }
                    }

                    evenementService.createAlertEvent(
                            type == EvenementType.OMISSION_PASTILLE ? "Omission pastille" : "Pastille manquante",
                            "Pastille " + pastilleAttendue.getPastille().getCode() + " non scannée",
                            type,
                            severity,
                            execRonde.getId(),
                            pastilleAttendue.getId()
                    );

                    manquantes++;

                    // Marquer comme manquée si délai important
                    if (pastilleAttendue.getExpectedTime() != null &&
                            LocalDateTime.now().isAfter(pastilleAttendue.getExpectedTime().plusHours(2))) {
                        pastilleAttendue.setStatus(Status_ronde_pastille.MISSED);
                        execRondePastilleRepository.save(pastilleAttendue);
                    }
                }
            }
        }

        return manquantes;
    }

    /**
     * Vérifie la séquence des pointages
     */
    private int verifierSequence(Exec_ronde execRonde) {
        List<Exec_ronde_pastille> pastilles = execRondePastilleRepository
                .findByExecRondeIdOrderBySeqNo(execRonde.getId());

        int erreurs = 0;
        int dernierSeqScanne = -1;
        LocalDateTime dernierTempsScan = null;

        for (Exec_ronde_pastille pastille : pastilles) {
            if (pastille.getStatus() == Status_ronde_pastille.DONE) {

                // Vérifier la séquence
                if (pastille.getSeqNo() <= dernierSeqScanne) {
                    evenementService.createAlertEvent(
                            "Séquence incorrecte",
                            "Pastille " + pastille.getPastille().getCode() +
                                    " (seq " + pastille.getSeqNo() + ") scannée hors ordre",
                            EvenementType.SEQUENCE_INCORRECTE,
                            EvenementSeverity.MOYENNE,
                            execRonde.getId(),
                            pastille.getId()
                    );
                    erreurs++;
                }

                // Vérifier le temps de trajet si on a le scan précédent
                if (dernierTempsScan != null && pastille.getExpectedTravelSec() != null) {
                    long tempsReel = Duration.between(dernierTempsScan, pastille.getScannedAt()).getSeconds();
                    long tempsAttendu = pastille.getExpectedTravelSec();

                    if (tempsReel > tempsAttendu * 1.5) { // 50% de dépassement
                        evenementService.createAlertEvent(
                                "Temps de trajet élevé",
                                "Temps réel: " + tempsReel + "s, attendu: " + tempsAttendu + "s",
                                EvenementType.TEMPS_TRAJET_ELEVE,
                                EvenementSeverity.FAIBLE,
                                execRonde.getId(),
                                pastille.getId()
                        );
                        erreurs++;
                    }
                }

                dernierSeqScanne = pastille.getSeqNo();
                dernierTempsScan = pastille.getScannedAt();
            }
        }

        return erreurs;
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
                                "Ronde " + ronde.getCode() + " générée",
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
                "Job exécuté",
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

                EvenementType type = delayMinutes > 30 ?
                        EvenementType.OMISSION_PASTILLE : EvenementType.PASTILLE_MANQUANTE;

                evenementService.createAlertEvent(
                        type == EvenementType.OMISSION_PASTILLE ? "Omission pastille" : "Pastille manquante",
                        "Pastille " + execPastille.getPastille().getCode() + " non scannée",
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

                EvenementType type = execPastille.getLateMinutes() > 30 ?
                        EvenementType.RETARD_IMPORTANT : EvenementType.RETARD_MODERE;

                EvenementSeverity severity = execPastille.getLateMinutes() > 30 ?
                        EvenementSeverity.ELEVEE : EvenementSeverity.MOYENNE;

                evenementService.createAlertEvent(
                        type == EvenementType.RETARD_IMPORTANT ? "Retard important" : "Retard modéré",
                        "Retard de " + execPastille.getLateMinutes() + " minutes",
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
                            "Séquence incorrecte",
                            "Pastille " + current.getPastille().getCode() + " scannée avant la pastille " + previous.getPastille().getCode(),
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

        if (scannedPastilles < totalPastilles) {
            EvenementSeverity severity = EvenementSeverity.FAIBLE;

            if (scannedPastilles < totalPastilles / 2) {
                severity = EvenementSeverity.ELEVEE;
            } else if (scannedPastilles < totalPastilles * 3 / 4) {
                severity = EvenementSeverity.MOYENNE;
            }

            evenementService.createAlertEvent(
                    "Ronde incomplète",
                    (totalPastilles - scannedPastilles) + " pastilles non scannées",
                    EvenementType.RONDE_INCOMPLETE,
                    severity,
                    execRonde.getId(),
                    null
            );

            return 1;
        } else {
            // Ronde complétée avec succès
            evenementService.createPositiveEvent(
                    "Ronde complétée",
                    "Toutes les pastilles ont été scannées",
                    EvenementType.RONDE_COMPLETEE,
                    execRonde.getId(),
                    null
            );
            execRonde.setStatus(Status_exec_Ronde.DONE);
            execRondeRepository.save(execRonde);
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
            incidentDTO.setDescription(missingPastilles + " pastilles manquantes");
            incidentDTO.setType(IncidentType.PASTILLE_MANQUANTE);
            incidentDTO.setSeverity(IncidentSeverity.ELEVEE);
            incidentDTO.setStatus(IncidentStatus.OUVERT);
            incidentDTO.setExecRondeId(execRonde.getId());
            incidentDTO.setSiteId(execRonde.getSite().getId());
            incidentDTO.setRondeId(execRonde.getRefRonde().getId());

            incidentService.createIncident(incidentDTO);
            incidentsCreated++;
        }

        // Incident: Retards importants multiples
        long importantDelays = execPastilles.stream()
                .filter(p -> p.getLateMinutes() != null && p.getLateMinutes() > 30)
                .count();

        if (importantDelays >= 3) {
            IncidentDTO incidentDTO = new IncidentDTO();
            incidentDTO.setTitle("Retards importants multiples");
            incidentDTO.setDescription(importantDelays + " pastilles avec retard >30min");
            incidentDTO.setType(IncidentType.RETARD);
            incidentDTO.setSeverity(IncidentSeverity.MOYENNE);
            incidentDTO.setStatus(IncidentStatus.OUVERT);
            incidentDTO.setExecRondeId(execRonde.getId());
            incidentDTO.setSiteId(execRonde.getSite().getId());
            incidentDTO.setRondeId(execRonde.getRefRonde().getId());

            incidentService.createIncident(incidentDTO);
            incidentsCreated++;
        }

        // Incident: Doubles scans répétés
        long doubleScans = evenementService.countEvenementsByExecRondeAndType(
                execRonde.getId(), EvenementType.DOUBLE_SCAN);

        if (doubleScans >= 3) {
            IncidentDTO incidentDTO = new IncidentDTO();
            incidentDTO.setTitle("Doubles scans multiples");
            incidentDTO.setDescription(doubleScans + " doubles scans détectés");
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
                "Erreur job",
                "Job " + job.getName() + " a échoué: " + errorMessage,
                EvenementType.AUTRE,
                EvenementSeverity.MOYENNE,
                null,
                null
        );
    }

    private void syncReferenceData(SysJobRun jobRun) {
        evenementService.createPositiveEvent(
                "Sync terminée",
                "Synchronisation des référentiels effectuée",
                EvenementType.SYNCHRONISATION_TERMINEE,
                null,
                null
        );
    }

    private void calculateDailyStats(SysJobRun jobRun) {
        evenementService.createPositiveEvent(
                "Stats calculées",
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

        evenementService.createAlertEvent(
                "Terminal inactif",
                "Certains terminaux n'ont pas envoyé de données depuis 24h",
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