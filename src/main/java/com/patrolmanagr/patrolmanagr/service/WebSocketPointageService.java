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

    private final ConcurrentLinkedQueue<WebSocketPointageDTO> pointageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger receivedCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public void receivePointage(WebSocketPointageDTO pointageDTO) {
        try {
            log.info("Pointage WebSocket reçu: externalUid={}", pointageDTO.getExternalUid());

            if (pointageDTO.getExternalUid() == null || pointageDTO.getExternalUid().trim().isEmpty()) {
                log.warn("Pointage rejeté: externalUid manquant");
                return;
            }

            pointageQueue.offer(pointageDTO);
            receivedCount.incrementAndGet();

            sendRealtimeNotification(pointageDTO);

            log.debug("Pointage ajouté. File d'attente: {}", pointageQueue.size());

        } catch (Exception e) {
            log.error("Erreur réception pointage: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processBatchEveryMinute() {
        if (pointageQueue.isEmpty()) {
            log.debug("Aucun pointage à traiter");
            return;
        }

        int queueSize = pointageQueue.size();
        log.info("Début traitement batch: {} pointage(s) en attente", queueSize);

        List<WebSocketPointageDTO> batch = new ArrayList<>();
        WebSocketPointageDTO pointage;
        while ((pointage = pointageQueue.poll()) != null) {
            batch.add(pointage);
        }

        if (batch.isEmpty()) {
            return;
        }

        List<String> externalUids = batch.stream()
                .map(WebSocketPointageDTO::getExternalUid)
                .filter(uid -> uid != null && !uid.trim().isEmpty())
                .distinct()
                .toList();

        Map<String, Ref_pastille> pastilleMap = loadPastilles(externalUids);

        List<FactPointageDTO> pointagesToSave = new ArrayList<>();
        int successCount = 0;
        int rejectedCount = 0;

        for (WebSocketPointageDTO wsPointage : batch) {
            try {
                FactPointageDTO enrichedPointage = convertAndEnrichPointage(wsPointage, pastilleMap);

                // ✅ PLUS AUCUNE VÉRIFICATION DE DOUBLON
                // On laisse tout passer, la base gérera les doublons via contrainte

                pointagesToSave.add(enrichedPointage);

                if ("PROCESSED".equals(enrichedPointage.getProcessedStatus())) {
                    successCount++;
                } else {
                    rejectedCount++;
                    log.warn("Pointage rejeté: {} - raison: {}",
                            wsPointage.getExternalUid(), enrichedPointage.getRejectionReason());
                }

            } catch (Exception e) {
                log.error("Erreur traitement pointage {}: {}",
                        wsPointage.getExternalUid(), e.getMessage());
                rejectedCount++;
            }
        }

        if (!pointagesToSave.isEmpty()) {
            try {
                List<Fact_pointage> savedPointages = factPointageService.savePointageBatch(pointagesToSave);
                processedCount.addAndGet(savedPointages.size());

                log.info("Batch terminé: {} sauvegardés ({} succès, {} rejets)",
                        savedPointages.size(), successCount, rejectedCount);

                sendBatchNotification(savedPointages.size(), batch.size(), successCount, rejectedCount);

            } catch (Exception e) {
                log.error("Erreur sauvegarde batch: {}", e.getMessage(), e);
            }
        }
    }

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

            log.debug("{} pastille(s) chargée(s) pour le batch", pastilles.size());

        } catch (Exception e) {
            log.error("Erreur chargement pastilles: {}", e.getMessage());
        }

        return pastilleMap;
    }

    private FactPointageDTO convertAndEnrichPointage(WebSocketPointageDTO wsPointage,
                                                     Map<String, Ref_pastille> pastilleMap) {
        FactPointageDTO dto = new FactPointageDTO();

        dto.setPastilleCodeRaw(wsPointage.getExternalUid());
        dto.setTerminalCodeRaw(wsPointage.getTerminalCode());
        dto.setAgentCodeRaw(wsPointage.getAgentCode());
        dto.setSourceType(Source_Type.GATEWAY);

        try {
            dto.setEventTime(LocalDateTime.parse(wsPointage.getTimestamp()));
        } catch (Exception e) {
            dto.setEventTime(LocalDateTime.now());
        }

        Ref_pastille pastille = pastilleMap.get(wsPointage.getExternalUid());

        if (pastille != null) {
            enrichWithPastilleInfo(dto, pastille);
            dto.setProcessedStatus("PROCESSED");
            log.debug("Pastille trouvée: {} (ID: {})", pastille.getExternal_uid(), pastille.getId());

        } else {
            handleMissingPastille(dto, wsPointage);
            log.warn("Pastille non trouvée: {}", wsPointage.getExternalUid());
        }

        return dto;
    }

    private void enrichWithPastilleInfo(FactPointageDTO dto, Ref_pastille pastille) {
        dto.setPastilleId(pastille.getId());
        dto.setPastilleLabel(pastille.getLabel());

        if (pastille.getRef_site_id() != null) {
            enrichWithSiteInfo(dto, pastille.getRef_site_id());
        }

        if (pastille.getRef_secteur_id() != null) {
            dto.setSecteurId(pastille.getRef_secteur_id().getId());
            dto.setSecteurName(pastille.getRef_secteur_id().getName());
        }
    }

    private void enrichWithSiteInfo(FactPointageDTO dto, Ref_site site) {
        dto.setSiteId(site.getId());
        dto.setSiteName(site.getName());

        if (site.getRef_zone() != null) {
            dto.setZoneId(site.getRef_zone().getId());
            dto.setZoneName(site.getRef_zone().getName());
        }

        try {
            Long rondeId = findActiveRondeForSite(site.getId());
            if (rondeId != null) {
                dto.setRondeId(rondeId);
                log.debug("   Ronde trouvée: {}", rondeId);
            }
        } catch (Exception e) {
            log.warn("   Aucune ronde trouvée pour le site {}", site.getId());
        }
    }

    private Long findActiveRondeForSite(Long siteId) {
        try {
            List<Ref_ronde> rondes = refRondeService.findRondeByIdSite(siteId);

            if (rondes.isEmpty()) {
                return null;
            }

            for (Ref_ronde ronde : rondes) {
                if (ronde.getStatus() != null && ronde.getStatus().name().equals("ACTIVE")) {
                    return ronde.getId();
                }
            }

            return rondes.get(0).getId();

        } catch (Exception e) {
            log.warn("Erreur recherche ronde site {}: {}", siteId, e.getMessage());
            return null;
        }
    }

    private void handleMissingPastille(FactPointageDTO dto, WebSocketPointageDTO wsPointage) {
        Long siteId = determineSiteId(wsPointage);
        dto.setSiteId(siteId);

        try {
            Ref_site site = refSiteService.findSiteById(siteId);
            dto.setSiteName(site.getName());
        } catch (Exception e) {
            dto.setSiteName("Site inconnu");
        }

        dto.setProcessedStatus("REJECTED");
        dto.setRejectionReason("Pastille non trouvée: " + wsPointage.getExternalUid());
    }

    private Long determineSiteId(WebSocketPointageDTO wsPointage) {
        try {
            if (wsPointage.getSiteCode() != null) {
                return Long.parseLong(wsPointage.getSiteCode());
            }
        } catch (Exception e) {
        }
        return 1L;
    }

    private void sendRealtimeNotification(WebSocketPointageDTO pointage) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("externalUid", pointage.getExternalUid());
            notification.put("terminalCode", pointage.getTerminalCode());
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("queueSize", pointageQueue.size());

            messagingTemplate.convertAndSend("/topic/pointages/realtime", notification);

        } catch (Exception e) {
            log.error("Erreur notification temps réel: {}", e.getMessage());
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
            log.error("Erreur notification batch: {}", e.getMessage());
        }
    }

    public int getQueueSize() { return pointageQueue.size(); }
    public int getTotalReceived() { return receivedCount.get(); }
    public int getTotalProcessed() { return processedCount.get(); }

    //  AJOUTE CETTE MÉTHODE ICI
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("queueSize", pointageQueue.size());
        stats.put("totalReceived", receivedCount.get());
        stats.put("totalProcessed", processedCount.get());
        stats.put("timestamp", LocalDateTime.now().toString());
        return stats;
    }
}