package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Source_Type;
import com.patrolmanagr.patrolmanagr.dto.FactPointageDTO;
import com.patrolmanagr.patrolmanagr.dto.WebSocketPointageDTO;
import com.patrolmanagr.patrolmanagr.entity.Fact_pointage;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class WebSocketPointageService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FactPointageService factPointageService;

    @Autowired
    private RefPastilleService refPastilleService;

    @Autowired
    private RefSiteService refSiteService;

    @Autowired
    private RefRondeService refRondeService;

    // File d'attente pour les pointages reçus
    private final ConcurrentLinkedQueue<WebSocketPointageDTO> pointageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger receivedCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);

    /**
     * Reçoit un pointage WebSocket et l'ajoute à la file d'attente
     */
    public void receivePointage(WebSocketPointageDTO pointageDTO) {
        try {
            log.info("📥 Pointage WebSocket reçu: externalUid={}", pointageDTO.getExternalUid());

            // Validation
            if (pointageDTO.getExternalUid() == null || pointageDTO.getExternalUid().trim().isEmpty()) {
                log.warn("❌ Pointage rejeté: externalUid manquant");
                return;
            }

            // Ajouter à la file
            pointageQueue.offer(pointageDTO);
            receivedCount.incrementAndGet();

            // Notification temps réel
            sendRealtimeNotification(pointageDTO);

            log.debug("📊 Pointage ajouté. File d'attente: {}", pointageQueue.size());

        } catch (Exception e) {
            log.error("❌ Erreur réception pointage: {}", e.getMessage(), e);
        }
    }

    /**
     * Traitement batch toutes les 1 minute
     */
    @Scheduled(fixedRate = 60000) // 60,000 ms = 1 minute
    @Transactional
    public void processBatchEveryMinute() {
        if (pointageQueue.isEmpty()) {
            log.debug("⏰ Aucun pointage à traiter");
            return;
        }

        int queueSize = pointageQueue.size();
        log.info("🔄 Début traitement batch: {} pointage(s) en attente", queueSize);

        // Extraire tous les pointages de la file
        List<WebSocketPointageDTO> batch = new ArrayList<>();
        WebSocketPointageDTO pointage;
        while ((pointage = pointageQueue.poll()) != null) {
            batch.add(pointage);
        }

        if (batch.isEmpty()) {
            return;
        }

        // Étape 1: Récupérer TOUS les external_uids pour optimisation
        List<String> externalUids = batch.stream()
                .map(WebSocketPointageDTO::getExternalUid)
                .filter(uid -> uid != null && !uid.trim().isEmpty())
                .distinct()
                .toList();

        // Étape 2: Charger toutes les pastilles correspondantes
        Map<String, Ref_pastille> pastilleMap = loadPastilles(externalUids);

        // Étape 3: Convertir et enrichir chaque pointage
        List<FactPointageDTO> pointagesToSave = new ArrayList<>();
        int successCount = 0;
        int rejectedCount = 0;

        for (WebSocketPointageDTO wsPointage : batch) {
            try {
                FactPointageDTO enrichedPointage = convertAndEnrichPointage(wsPointage, pastilleMap);
                pointagesToSave.add(enrichedPointage);

                if ("PROCESSED".equals(enrichedPointage.getProcessedStatus())) {
                    successCount++;
                } else {
                    rejectedCount++;
                    log.warn("❌ Pointage rejeté: {} - raison: {}",
                            wsPointage.getExternalUid(), enrichedPointage.getRejectionReason());
                }

            } catch (Exception e) {
                log.error("❌ Erreur traitement pointage {}: {}",
                        wsPointage.getExternalUid(), e.getMessage());
                rejectedCount++;
            }
        }

        // Étape 4: Sauvegarder en batch dans la base
        if (!pointagesToSave.isEmpty()) {
            try {
                List<Fact_pointage> savedPointages = factPointageService.savePointageBatch(pointagesToSave);
                processedCount.addAndGet(savedPointages.size());

                log.info("✅ Batch terminé: {} sauvegardés ({} succès, {} rejets)",
                        savedPointages.size(), successCount, rejectedCount);

                // Notification
                sendBatchNotification(savedPointages.size(), batch.size(), successCount, rejectedCount);

            } catch (Exception e) {
                log.error("❌ Erreur sauvegarde batch: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Charge toutes les pastilles correspondant aux external_uids
     */
    private Map<String, Ref_pastille> loadPastilles(List<String> externalUids) {
        Map<String, Ref_pastille> pastilleMap = new HashMap<>();

        if (externalUids.isEmpty()) {
            return pastilleMap;
        }

        try {
            List<Ref_pastille> pastilles = refPastilleService.findPastillesByExternalUids(externalUids);

            for (Ref_pastille pastille : pastilles) {
                if (pastille.getExternal_uid() != null) {
                    pastilleMap.put(pastille.getExternal_uid(), pastille);
                }
            }

            log.debug("📋 {} pastille(s) chargée(s) pour le batch", pastilles.size());

        } catch (Exception e) {
            log.error("❌ Erreur chargement pastilles: {}", e.getMessage());
        }

        return pastilleMap;
    }

    /**
     * Convertit un pointage WebSocket en FactPointageDTO enrichi
     */
    private FactPointageDTO convertAndEnrichPointage(WebSocketPointageDTO wsPointage,
                                                     Map<String, Ref_pastille> pastilleMap) {
        FactPointageDTO dto = new FactPointageDTO();

        // 1. Données de base
        dto.setPastilleCodeRaw(wsPointage.getExternalUid());
        dto.setTerminalCodeRaw(wsPointage.getTerminalCode());
        dto.setAgentCodeRaw(wsPointage.getAgentCode());
        dto.setSourceType(Source_Type.GATEWAY);

        // 2. Timestamp
        try {
            dto.setEventTime(LocalDateTime.parse(wsPointage.getTimestamp()));
        } catch (Exception e) {
            dto.setEventTime(LocalDateTime.now());
        }

        // 3. AUTO-ENRICHISSEMENT: Chercher la pastille
        Ref_pastille pastille = pastilleMap.get(wsPointage.getExternalUid());

        if (pastille != null) {
            // ✅ PASTILLE TROUVÉE - RÉCUPÉRER TOUTES LES INFOS
            enrichWithPastilleInfo(dto, pastille);
            dto.setProcessedStatus("PROCESSED");

        } else {
            // ❌ PASTILLE NON TROUVÉE - REJET
            handleMissingPastille(dto, wsPointage);
        }

        return dto;
    }

    /**
     * Enrichit avec les informations de la pastille
     */
    private void enrichWithPastilleInfo(FactPointageDTO dto, Ref_pastille pastille) {
        log.debug("✅ Pastille trouvée: {} (ID: {})", pastille.getExternal_uid(), pastille.getId());

        // 1. Informations de la pastille
        dto.setPastilleId(pastille.getId());
        dto.setPastilleLabel(pastille.getLabel());

        // 2. Site (via pastille)
        if (pastille.getRef_site_id() != null) {
            enrichWithSiteInfo(dto, pastille.getRef_site_id());
        }

        // 3. Secteur (via pastille)
        if (pastille.getRef_secteur_id() != null) {
            dto.setSecteurId(pastille.getRef_secteur_id().getId());
            dto.setSecteurName(pastille.getRef_secteur_id().getName());
        }
    }

    /**
     * Enrichit avec les informations du site
     */
    private void enrichWithSiteInfo(FactPointageDTO dto, Ref_site site) {
        dto.setSiteId(site.getId());
        dto.setSiteName(site.getName());

        // Zone (via site)
        if (site.getRef_zone() != null) {
            dto.setZoneId(site.getRef_zone().getId());
            dto.setZoneName(site.getRef_zone().getName());
        }

        // Ronde active du site
        try {
            Long rondeId = findActiveRondeForSite(site.getId());
            if (rondeId != null) {
                dto.setRondeId(rondeId);
                log.debug("   ↳ Ronde trouvée: {}", rondeId);
            }
        } catch (Exception e) {
            log.warn("   ↳ Aucune ronde trouvée pour le site {}", site.getId());
        }
    }

    /**
     * Trouve une ronde active pour un site
     */
    private Long findActiveRondeForSite(Long siteId) {
        try {
            List<Ref_ronde> rondes = refRondeService.findRondeByIdSite(siteId);

            if (rondes.isEmpty()) {
                return null;
            }

            // Chercher une ronde active
            for (Ref_ronde ronde : rondes) {
                if (ronde.getStatus() != null && ronde.getStatus().name().equals("ACTIVE")) {
                    return ronde.getId();
                }
            }

            // Sinon, première ronde
            return rondes.get(0).getId();

        } catch (Exception e) {
            log.warn("Erreur recherche ronde site {}: {}", siteId, e.getMessage());
            return null;
        }
    }

    /**
     * Gère le cas où la pastille n'est pas trouvée
     */
    private void handleMissingPastille(FactPointageDTO dto, WebSocketPointageDTO wsPointage) {
        log.warn("❌ Pastille non trouvée: {}", wsPointage.getExternalUid());

        // Essayer de déterminer le site
        Long siteId = determineSiteId(wsPointage);
        dto.setSiteId(siteId);

        // Récupérer les infos du site si possible
        try {
            Ref_site site = refSiteService.findSiteById(siteId);
            dto.setSiteName(site.getName());
        } catch (Exception e) {
            dto.setSiteName("Site inconnu");
        }

        dto.setProcessedStatus("REJECTED");
        dto.setRejectionReason("Pastille inconnue: " + wsPointage.getExternalUid());
    }

    /**
     * Détermine l'ID du site
     */
    private Long determineSiteId(WebSocketPointageDTO wsPointage) {
        try {
            // Si siteCode est fourni
            if (wsPointage.getSiteCode() != null) {
                return Long.parseLong(wsPointage.getSiteCode());
            }
        } catch (Exception e) {
            // Ignorer
        }

        // Site par défaut
        return 1L;
    }

    /**
     * Notifications
     */
    private void sendRealtimeNotification(WebSocketPointageDTO pointage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("externalUid", pointage.getExternalUid());
            notification.put("terminalCode", pointage.getTerminalCode());
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("queueSize", pointageQueue.size());

            messagingTemplate.convertAndSend("/topic/pointages/realtime", notification);

        } catch (Exception e) {
            log.error("❌ Erreur notification temps réel: {}", e.getMessage());
        }
    }

    private void sendBatchNotification(int savedCount, int batchSize, int successCount, int rejectedCount) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("savedCount", savedCount);
            notification.put("batchSize", batchSize);
            notification.put("successCount", successCount);
            notification.put("rejectedCount", rejectedCount);
            notification.put("totalProcessed", processedCount.get());

            messagingTemplate.convertAndSend("/topic/pointages/batch", notification);

        } catch (Exception e) {
            log.error("❌ Erreur notification batch: {}", e.getMessage());
        }
    }

    // Getters pour monitoring
    public int getQueueSize() { return pointageQueue.size(); }
    public int getTotalReceived() { return receivedCount.get(); }
    public int getTotalProcessed() { return processedCount.get(); }

    /**
     * Ajoute un pointage de test
     */
    public void addTestPointage(String externalUid) {
        WebSocketPointageDTO testPointage = new WebSocketPointageDTO();
        testPointage.setExternalUid(externalUid);
        testPointage.setTerminalCode("TEST-TERMINAL-" + System.currentTimeMillis() % 1000);
        testPointage.setAgentCode("TEST-AGENT");
        testPointage.setTimestamp(LocalDateTime.now().toString());
        testPointage.setRawData("{\"test\": true}");

        receivePointage(testPointage);
    }
}