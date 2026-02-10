package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.JobSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/scheduler/*")
@Slf4j
public class JobSchedulerController {

    @Autowired
    private JobSchedulerService jobSchedulerService;

    /**
     * Initialiser/Recharger tous les jobs
     */
    @PostMapping("/initialize")
    public ResponseEntity<ResponseMessage> initializeScheduler() {
        try {
            jobSchedulerService.initializeScheduledJobs();
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Scheduler initialisé avec succès. Tous les jobs actifs ont été programmés.",
                            null),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation du scheduler: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur lors de l'initialisation: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Exécuter manuellement les rondes du jour
     */
    @PostMapping("/execute-daily")
    public ResponseEntity<ResponseMessage> executeDailyRondes() {
        try {
            jobSchedulerService.executeDailyRondes();
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Exécution quotidienne des rondes déclenchée avec succès",
                            null),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution quotidienne: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Obtenir la liste des jobs programmés
     */
    @GetMapping("/jobs")
    public ResponseEntity<ResponseMessage> getScheduledJobs() {
        try {
            List<Map<String, Object>> jobs = jobSchedulerService.getScheduledJobs();
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Jobs programmés (" + jobs.size() + ")",
                            jobs),
                    HttpStatus.OK
            );
        } catch (SchedulerException e) {
            log.error("Erreur lors de la récupération des jobs: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur Scheduler: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des jobs: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Obtenir l'état du scheduler
     */
    @GetMapping("/status")
    public ResponseEntity<ResponseMessage> getSchedulerStatus() {
        try {
            Map<String, Object> status = jobSchedulerService.getSchedulerStatus();
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "État du scheduler",
                            status),
                    HttpStatus.OK
            );
        } catch (SchedulerException e) {
            log.error("Erreur lors de la récupération du statut: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur Scheduler: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Déclencher l'exécution d'un job spécifique
     */
    @PostMapping("/trigger-job/{jobId}")
    public ResponseEntity<ResponseMessage> triggerJob(@PathVariable Long jobId) {
        try {
            var result = jobSchedulerService.executeJobManually(jobId);
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Job déclenché manuellement avec succès",
                            result),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            log.error("Erreur lors du déclenchement du job: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Redémarrer le scheduler
     */
    @PostMapping("/restart")
    public ResponseEntity<ResponseMessage> restartScheduler() {
        try {
            jobSchedulerService.restartScheduler();
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Scheduler redémarré avec succès",
                            null),
                    HttpStatus.OK
            );
        } catch (SchedulerException e) {
            log.error("Erreur lors du redémarrage du scheduler: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur Scheduler: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("Erreur lors du redémarrage du scheduler: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Reprogrammer un job spécifique
     */
    @PostMapping("/reschedule/{jobId}")
    public ResponseEntity<ResponseMessage> rescheduleJob(@PathVariable Long jobId) {
        try {
            jobSchedulerService.rescheduleJob(jobId);
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Job reprogrammé avec succès",
                            null),
                    HttpStatus.OK
            );
        } catch (SchedulerException e) {
            log.error("Erreur lors de la reprogrammation du job: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur Scheduler: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("Erreur lors de la reprogrammation du job: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Désactiver un job programmé
     */
    @PostMapping("/unschedule/{jobId}")
    public ResponseEntity<ResponseMessage> unscheduleJob(@PathVariable Long jobId) {
        try {
            jobSchedulerService.unscheduleJob(jobId);
            return new ResponseEntity<>(
                    new ResponseMessage("success",
                            "Job désactivé du scheduler",
                            null),
                    HttpStatus.OK
            );
        } catch (SchedulerException e) {
            log.error("Erreur lors de la désactivation du job: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur Scheduler: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            log.error("Erreur lors de la désactivation du job: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ResponseMessage("error",
                            "Erreur: " + e.getMessage(),
                            null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}