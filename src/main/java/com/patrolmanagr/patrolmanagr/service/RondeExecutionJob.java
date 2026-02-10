package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.JobRunStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
public class RondeExecutionJob implements Job, ApplicationContextAware {

    private static ApplicationContext staticApplicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        staticApplicationContext = applicationContext;
        log.info("ApplicationContext stocké dans RondeExecutionJob");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("=== EXÉCUTION DU JOB QUARTZ ===");

        Long jobId = null;
        String jobCode = "UNKNOWN";
        Long jobRunId = null;

        try {
            // Récupérer les paramètres
            jobId = context.getJobDetail().getJobDataMap().getLong("jobId");
            jobCode = context.getJobDetail().getJobDataMap().getString("jobCode");
            String jobName = context.getJobDetail().getJobDataMap().getString("jobName");

            log.info("🚀 Démarrage du job Quartz: {} ({}) - ID: {}", jobCode, jobName, jobId);
            log.info("Déclenché par: {}", context.getTrigger().getKey());

            // Vérifier que ApplicationContext est disponible
            if (staticApplicationContext == null) {
                throw new IllegalStateException("ApplicationContext non disponible!");
            }

            // Récupérer les services depuis ApplicationContext
            RondeExecutionService rondeExecutionService = staticApplicationContext.getBean(RondeExecutionService.class);
            SysJobRunService sysJobRunService = staticApplicationContext.getBean(SysJobRunService.class);

            log.info("✅ Services récupérés depuis ApplicationContext");

            // Démarrer un job run
            var jobRun = sysJobRunService.startJobRun(jobId);
            jobRunId = jobRun.getId();
            log.info("JobRun créé: {}", jobRunId);

            // Exécuter le job métier
            var result = rondeExecutionService.executeJob(jobId);
            log.info("Résultat de l'exécution: {}", result);

            // Calculer et enregistrer la durée
            jobRun.setDurationMs(System.currentTimeMillis() - context.getFireTime().getTime());

            // Mettre à jour le job run avec succès
            sysJobRunService.completeJobRun(jobRunId, JobRunStatus.OK,
                    "Job exécuté avec succès - Résultat: " + result);

            log.info("✅ Job Quartz {} exécuté avec succès", jobCode);

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'exécution du job Quartz {}: {}", jobCode, e.getMessage(), e);

            // Mettre à jour le job run en échec
            if (jobRunId != null && staticApplicationContext != null) {
                try {
                    SysJobRunService sysJobRunService = staticApplicationContext.getBean(SysJobRunService.class);
                    sysJobRunService.completeJobRun(jobRunId, JobRunStatus.ERROR,
                            "Erreur: " + e.getMessage());
                    log.info("Statut du job run mis à jour en échec");
                } catch (Exception ex) {
                    log.error("Impossible de mettre à jour le statut du job run: {}", ex.getMessage());
                }
            }

            // Relancer l'exception pour que Quartz la gère
            JobExecutionException jobException = new JobExecutionException(e);
            jobException.setRefireImmediately(false);
            throw jobException;
        }
    }
}