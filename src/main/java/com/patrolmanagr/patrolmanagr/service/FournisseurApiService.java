package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.WebSocketPointageDTO;
import com.patrolmanagr.patrolmanagr.repository.FactPointageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class FournisseurApiService {

    @Value("${fournisseur.api.base-url:http://localhost:3005}")
    private String apiBaseUrl;

    @Value("${fournisseur.api.key:}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebSocketPointageService webSocketPointageService;

    @Autowired
    private FactPointageRepository factPointageRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private LocalDateTime lastPointageTimestamp = null;

    private static final List<String> EXTERNAL_UID_KEYS = Arrays.asList(
            "external_uid", "externalUid", "uid", "device_id", "deviceId",
            "pastille_id", "pastilleId", "id", "badge_id", "badgeId"
    );

    private static final List<String> TIMESTAMP_KEYS = Arrays.asList(
            "event_time", "timestamp", "eventTime", "created_at", "date", "time", "scanned_at"
    );

    // =============== MÉTHODE PRINCIPALE ===============
    @Scheduled(fixedDelay = 60000)
    public void pollFournisseurApi() {
        try {
            // 1. Initialisation du timestamp
            if (lastPointageTimestamp == null) {
                LocalDateTime lastInDb = factPointageRepository.findLastPointageTime();
                if (lastInDb != null) {
                    lastPointageTimestamp = lastInDb.plusNanos(1);
                    log.info("📅 [API] Timestamp initialisé depuis base: {} +1ns", lastInDb);
                } else {
                    lastPointageTimestamp = LocalDateTime.now().minusDays(7);
                    log.info("📅 [API] Aucun pointage en base - Démarrage J-7");
                }
            }

            // 2. Construction URL
            String url = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/pointages")
                    .queryParam("from", lastPointageTimestamp.format(formatter))
                    .queryParam("limit", 100)
                    .build()
                    .toUriString();

            // 3. Appel API
            FournisseurApiResponse response = callFournisseurApi(url);

            // 4. Traitement des données
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Object> data = response.getData();

                if (!data.isEmpty()) {
                    log.info("📦 [API] {} pointages reçus du fournisseur", data.size());

                    // 5. FILTRAGE : ne garder que les NOUVEAUX pointages
                    int nouveaux = filterAndProcessNewPointages(data);

                    if (nouveaux > 0) {
                        log.info("✅ [API] {} NOUVEAUX pointages traités", nouveaux);

                        // 6. Mise à jour du timestamp depuis la BASE
                        LocalDateTime dernier = factPointageRepository.findLastPointageTime();
                        if (dernier != null) {
                            lastPointageTimestamp = dernier.plusNanos(1);
                            log.debug("📅 [API] Timestamp mis à jour: {}", lastPointageTimestamp);
                        }
                    } else {
                        log.info("📭 [API] Aucun nouveau pointage - tous déjà en base");
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ [API] Erreur polling: {}", e.getMessage(), e);
        }
    }

    // =============== FILTRAGE DES NOUVEAUX POINTAGES ===============
    private int filterAndProcessNewPointages(List<Object> data) {
        int nouveaux = 0;

        // Récupérer le dernier timestamp en BASE (référence absolue)
        LocalDateTime dernierEnBase = factPointageRepository.findLastPointageTime();
        if (dernierEnBase == null) {
            dernierEnBase = LocalDateTime.now().minusDays(7);
        }

        for (Object item : data) {
            try {
                String externalUid = extractExternalUid(item);
                if (externalUid == null || externalUid.isEmpty()) {
                    continue;
                }

                String timestampStr = extractTimestamp(item);
                if (timestampStr == null) {
                    log.debug("⏭️ Pointage ignoré - timestamp manquant: {}", externalUid);
                    continue;
                }

                LocalDateTime pointageTime = LocalDateTime.parse(timestampStr, formatter);

                // ✅ UNIQUEMENT les pointages STRICTEMENT PLUS RÉCENTS que le dernier en base
                if (pointageTime.isAfter(dernierEnBase)) {
                    WebSocketPointageDTO pointage = new WebSocketPointageDTO();
                    pointage.setExternalUid(externalUid);
                    pointage.setTimestamp(timestampStr);

                    // Extraire les champs optionnels si présents
                    if (item instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) item;
                        Object terminalCode = map.get("terminal_code");
                        if (terminalCode == null) terminalCode = map.get("terminalCode");
                        if (terminalCode != null) pointage.setTerminalCode(terminalCode.toString());

                        Object agentCode = map.get("agent_code");
                        if (agentCode == null) agentCode = map.get("agentCode");
                        if (agentCode != null) pointage.setAgentCode(agentCode.toString());

                        Object siteCode = map.get("site_code");
                        if (siteCode == null) siteCode = map.get("siteCode");
                        if (siteCode == null) siteCode = map.get("site_id");
                        if (siteCode != null) pointage.setSiteCode(siteCode.toString());

                        pointage.setRawData(map.toString());
                    }

                    webSocketPointageService.receivePointage(pointage);
                    nouveaux++;
                    log.debug("✅ NOUVEAU pointage: {} - {}", externalUid, pointageTime);
                } else {
                    log.debug("⏭️ Ancien pointage ignoré (déjà en base): {} - {}", externalUid, pointageTime);
                }

            } catch (Exception e) {
                log.error("❌ [API] Erreur filtrage: {}", e.getMessage());
            }
        }

        return nouveaux;
    }

    // =============== APPEL API ===============
    private FournisseurApiResponse callFournisseurApi(String url) {
        try {
            log.debug("📡 [API] Appel: {}", url);

            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<FournisseurApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, FournisseurApiResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("❌ [API] Échec appel: {}", e.getMessage());
            return null;
        }
    }

    // =============== EXTRACTION EXTERNAL_UID ===============
    private String extractExternalUid(Object obj) {
        if (!(obj instanceof Map)) return null;
        Map<?, ?> map = (Map<?, ?>) obj;
        for (String key : EXTERNAL_UID_KEYS) {
            Object value = map.get(key);
            if (value != null) {
                String uid = value.toString().trim();
                if (!uid.isEmpty()) return uid;
            }
        }
        return null;
    }

    // =============== EXTRACTION TIMESTAMP ===============
    private String extractTimestamp(Object obj) {
        if (!(obj instanceof Map)) return null;
        Map<?, ?> map = (Map<?, ?>) obj;
        for (String key : TIMESTAMP_KEYS) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    // =============== CLASSE DE RÉPONSE API ===============
    public static class FournisseurApiResponse {
        private boolean success;
        private List<Object> data;
        private int total;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<Object> getData() { return data; }
        public void setData(List<Object> data) { this.data = data; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}