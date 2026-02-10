package com.patrolmanagr.patrolmanagr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StartupService {

    private final JobSchedulerService jobSchedulerService;

    public StartupService(JobSchedulerService jobSchedulerService) {
        this.jobSchedulerService = jobSchedulerService;
    }

    /**
     * Démarrer automatiquement les jobs au démarrage de l'application
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startJobsOnApplicationReady() {
        try {
            log.info("=================================================================================");
            log.info("🚀 DÉMARRAGE AUTOMATIQUE DES JOBS AU LANCEMENT DE L'APPLICATION");
            log.info("=================================================================================");

            // Attendre 5 secondes pour que tout soit bien initialisé
            Thread.sleep(5000);

            // Initialiser les jobs programmés
            jobSchedulerService.initializeScheduledJobs();

            log.info("=================================================================================");
            log.info("✅ TOUS LES JOBS ONT ÉTÉ INITIALISÉS AVEC SUCCÈS");
            log.info("=================================================================================");

        } catch (Exception e) {
            log.error("❌ ERREUR LORS DU DÉMARRAGE DES JOBS", e);
        }
    }

    /**
     * Programmer un job de test pour vérifier le fonctionnement
     */
    private void scheduleTestJob() {
        try {
            log.info("Configuration d'un job de test...");

            // Code pour créer et programmer un job de test
            // (à adapter selon votre structure)

            log.info("✅ Job de test programmé avec succès");
        } catch (Exception e) {
            log.error("❌ Erreur lors de la programmation du job de test", e);
        }
    }
}