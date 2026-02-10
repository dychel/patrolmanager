package com.patrolmanagr.patrolmanagr.config;

import com.patrolmanagr.patrolmanagr.service.JobSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulerAutoStartConfig {

    @Autowired
    private JobSchedulerService jobSchedulerService;

    /**
     * Démarrer le scheduler après le démarrage de Spring
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startSchedulerOnStartup() {
        log.info("===========================================");
        log.info("Démarrage du scheduler Quartz...");
        log.info("===========================================");

        try {
            // Attendre que Spring soit complètement initialisé
            Thread.sleep(5000);
            jobSchedulerService.initializeScheduledJobs();
            log.info("✅ Scheduler démarré avec succès");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interruption lors du démarrage", e);
        } catch (Exception e) {
            log.error("Erreur démarrage scheduler: {}", e.getMessage());
        }
    }
}