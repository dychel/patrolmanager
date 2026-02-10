package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.ScheduleTypeJob;
import com.patrolmanagr.patrolmanagr.entity.SysJob;
import com.patrolmanagr.patrolmanagr.entity.SysJobRun;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.SysJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class JobSchedulerService {

    @Autowired
    private SysJobRepository sysJobRepository;

    @Autowired
    private RondeExecutionService rondeExecutionService;

    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    // Injecter le Scheduler via setter pour éviter les problèmes d'initialisation
    private Scheduler scheduler;

    @Autowired
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        log.info("Scheduler injecté dans JobSchedulerService: {}", scheduler != null ? "OK" : "NULL");
    }

    private static final String JOB_GROUP = "RONDE_JOBS";
    private static final String TRIGGER_GROUP = "RONDE_TRIGGERS";

    /**
     * Initialiser tous les jobs actifs au démarrage
     */
    public void initializeScheduledJobs() {
        if (isInitialized.getAndSet(true)) {
            log.info("Scheduler déjà initialisé");
            return;
        }

        try {
            log.info("=== Initialisation des jobs programmés ===");

            // Vérifier que le scheduler est disponible
            if (scheduler == null) {
                log.error("Scheduler non disponible - injection échouée");
                isInitialized.set(false);
                return;
            }

            // Récupérer tous les jobs actifs
            List<SysJob> activeJobs = sysJobRepository.findByIsEnabledTrue();
            log.info("{} jobs actifs trouvés", activeJobs.size());

            // Programmer chaque job
            int scheduledCount = 0;
            for (SysJob job : activeJobs) {
                try {
                    if (job.getScheduleTypeJob() != ScheduleTypeJob.MANUEL) {
                        scheduleJob(job);
                        scheduledCount++;
                        log.info("✓ Job programmé: {} ({})", job.getJobCode(), job.getScheduleTypeJob());
                    }
                } catch (Exception e) {
                    log.error("✗ Erreur programmation job {}: {}", job.getJobCode(), e.getMessage());
                }
            }

            log.info("=== {} jobs programmés avec succès ===", scheduledCount);

        } catch (Exception e) {
            log.error("Erreur initialisation jobs: {}", e.getMessage(), e);
            isInitialized.set(false);
        }
    }

    /**
     * Programmer un job
     */
    public void scheduleJob(SysJob job) throws SchedulerException {
        if (!Boolean.TRUE.equals(job.getIsEnabled()) || job.getScheduleTypeJob() == ScheduleTypeJob.MANUEL) {
            return;
        }

        // Supprimer l'ancien job
        unscheduleJob(job.getId());

        // Créer le JobDetail
        JobDetail jobDetail = JobBuilder.newJob(RondeExecutionJob.class)
                .withIdentity(job.getJobCode(), JOB_GROUP)
                .withDescription(job.getDescription())
                .usingJobData("jobId", job.getId())
                .usingJobData("jobCode", job.getJobCode())
                .usingJobData("jobName", job.getName())
                .storeDurably()
                .build();

        // Créer le trigger selon le type
        Trigger trigger = createTriggerForJob(job);

        if (trigger == null) {
            return;
        }

        // Programmer le job
        scheduler.scheduleJob(jobDetail, trigger);

        log.info("Job {} programmé avec trigger: {}", job.getJobCode(), trigger.getKey());
    }

    /**
     * Créer un trigger pour le job
     */
    private Trigger createTriggerForJob(SysJob job) {
        try {
            switch (job.getScheduleTypeJob()) {
                case DAILY:
                    return createDailyTrigger(job);

                case HOURLY:
                    return createHourlyTrigger(job);

                case WEEKLY:
                    return createWeeklyTrigger(job);

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Erreur création trigger pour job {}: {}", job.getJobCode(), e.getMessage());
            return null;
        }
    }

    /**
     * Créer un trigger quotidien
     */
    private Trigger createDailyTrigger(SysJob job) {
        if (job.getScheduleTime() == null) {
            log.error("Job DAILY {} sans heure", job.getJobCode());
            return null;
        }

        String cronExpression = String.format("0 %d %d * * ?",
                job.getScheduleTime().getMinute(),
                job.getScheduleTime().getHour());

        return TriggerBuilder.newTrigger()
                .withIdentity(job.getJobCode() + "_DAILY", TRIGGER_GROUP)
                .withDescription("Quotidien à " + job.getScheduleTime())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .forJob(job.getJobCode(), JOB_GROUP)
                .build();
    }

    /**
     * Créer un trigger horaire
     */
    private Trigger createHourlyTrigger(SysJob job) {
        Integer interval = job.getScheduleIntervalMin() != null ? job.getScheduleIntervalMin() : 60;

        String cronExpression = String.format("0 0/%d * * * ?", interval);

        return TriggerBuilder.newTrigger()
                .withIdentity(job.getJobCode() + "_HOURLY", TRIGGER_GROUP)
                .withDescription("Toutes les " + interval + " minutes")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .forJob(job.getJobCode(), JOB_GROUP)
                .build();
    }

    /**
     * Créer un trigger hebdomadaire
     */
    private Trigger createWeeklyTrigger(SysJob job) {
        if (job.getScheduleTime() == null) {
            log.error("Job WEEKLY {} sans heure", job.getJobCode());
            return null;
        }

        // Par défaut: Lundi à Vendredi
        String cronExpression = String.format("0 %d %d ? * MON-FRI",
                job.getScheduleTime().getMinute(),
                job.getScheduleTime().getHour());

        return TriggerBuilder.newTrigger()
                .withIdentity(job.getJobCode() + "_WEEKLY", TRIGGER_GROUP)
                .withDescription("Hebdomadaire à " + job.getScheduleTime())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .forJob(job.getJobCode(), JOB_GROUP)
                .build();
    }

    /**
     * Désactiver un job
     */
    public void unscheduleJob(Long jobId) throws SchedulerException {
        SysJob job = sysJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        JobKey jobKey = JobKey.jobKey(job.getJobCode(), JOB_GROUP);

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            log.info("Job {} désactivé", job.getJobCode());
        }
    }

    /**
     * Reprogrammer un job
     */
    public void rescheduleJob(Long jobId) throws SchedulerException {
        SysJob job = sysJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        unscheduleJob(jobId);

        if (Boolean.TRUE.equals(job.getIsEnabled()) && job.getScheduleTypeJob() != ScheduleTypeJob.MANUEL) {
            scheduleJob(job);
            log.info("Job {} reprogrammé", job.getJobCode());
        }
    }

    /**
     * Exécuter un job manuellement
     */
    public SysJobRun executeJobManually(Long jobId) {
        SysJob job = sysJobRepository.findById(jobId)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        log.info("🚀 Exécution manuelle du job: {}", job.getJobCode());
        return rondeExecutionService.executeJobManually(jobId);
    }

    /**
     * Exécuter les rondes quotidiennes (méthode pour le controller)
     */
    public void executeDailyRondes() {
        try {
            log.info("=== Début de l'exécution quotidienne des rondes ===");

            // Récupérer tous les jobs actifs de type DAILY
            List<SysJob> dailyJobs = sysJobRepository.findByScheduleTypeJob(ScheduleTypeJob.DAILY)
                    .stream()
                    .filter(job -> Boolean.TRUE.equals(job.getIsEnabled()))
                    .collect(Collectors.toList());

            log.info("{} jobs DAILY actifs trouvés", dailyJobs.size());

            int executedJobs = 0;

            for (SysJob job : dailyJobs) {
                try {
                    // Vérifier l'heure d'exécution
                    if (shouldExecuteNow(job)) {
                        log.info("▶ Exécution du job {} prévue à {}",
                                job.getJobCode(), job.getScheduleTime());
                        executeJobManually(job.getId());
                        executedJobs++;
                    }
                } catch (Exception e) {
                    log.error("❌ Erreur lors de l'exécution du job {}: {}",
                            job.getJobCode(), e.getMessage());
                }
            }

            log.info("=== Exécution quotidienne terminée: {} jobs exécutés ===", executedJobs);

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'exécution quotidienne: {}", e.getMessage(), e);
        }
    }

    /**
     * Vérifier si c'est le moment d'exécuter le job
     */
    private boolean shouldExecuteNow(SysJob job) {
        if (job.getScheduleTime() == null) {
            log.warn("Job {} n'a pas d'heure de programmation", job.getJobCode());
            return false;
        }

        LocalTime now = LocalTime.now();
        LocalTime scheduleTime = job.getScheduleTime();

        // Exécuter si c'est l'heure (avec une marge de 5 minutes)
        boolean shouldExecute = now.isAfter(scheduleTime.minusMinutes(5)) &&
                now.isBefore(scheduleTime.plusMinutes(5));

        if (shouldExecute) {
            log.debug("Heure d'exécution: {} (maintenant: {})", scheduleTime, now);
        }

        return shouldExecute;
    }

    /**
     * Obtenir la liste des jobs programmés
     */
    public List<Map<String, Object>> getScheduledJobs() throws SchedulerException {
        List<Map<String, Object>> jobs = new ArrayList<>();

        if (scheduler == null) {
            log.error("Scheduler non disponible");
            return jobs;
        }

        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
            Map<String, Object> jobInfo = new HashMap<>();
            jobInfo.put("jobKey", jobKey.toString());
            jobInfo.put("name", jobKey.getName());
            jobInfo.put("group", jobKey.getGroup());

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            jobInfo.put("triggerCount", triggers.size());

            List<Map<String, Object>> triggerInfos = new ArrayList<>();
            for (Trigger trigger : triggers) {
                Map<String, Object> triggerInfo = new HashMap<>();
                triggerInfo.put("key", trigger.getKey().toString());
                triggerInfo.put("nextFireTime", trigger.getNextFireTime());
                triggerInfos.add(triggerInfo);
            }

            jobInfo.put("triggers", triggerInfos);
            jobs.add(jobInfo);
        }

        return jobs;
    }

    /**
     * Vérifier l'état du scheduler
     */
    public Map<String, Object> getSchedulerStatus() throws SchedulerException {
        Map<String, Object> status = new HashMap<>();

        if (scheduler == null) {
            status.put("isStarted", false);
            status.put("schedulerName", "NON_DISPONIBLE");
            status.put("jobCount", 0);
            status.put("initialized", false);
            return status;
        }

        status.put("isStarted", scheduler.isStarted());
        status.put("schedulerName", scheduler.getSchedulerName());
        status.put("jobCount", scheduler.getJobKeys(GroupMatcher.anyJobGroup()).size());
        status.put("initialized", isInitialized.get());

        return status;
    }

    /**
     * Redémarrer le scheduler
     */
    public void restartScheduler() throws SchedulerException {
        if (scheduler == null) {
            log.error("Impossible de redémarrer: scheduler non disponible");
            return;
        }

        log.info("Redémarrage du scheduler...");
        scheduler.shutdown();

        try {
            Thread.sleep(2000);
            scheduler.start();
            isInitialized.set(false);
            initializeScheduledJobs();
            log.info("Scheduler redémarré avec succès");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interruption lors du redémarrage", e);
        } catch (Exception e) {
            log.error("Erreur lors du redémarrage: {}", e.getMessage());
        }
    }

    /**
     * Obtenir le scheduler (pour le controller)
     */
    public Scheduler getScheduler() {
        return scheduler;
    }
}