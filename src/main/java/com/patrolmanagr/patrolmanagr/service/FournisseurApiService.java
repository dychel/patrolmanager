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

    // COMPTEUR = nombre de pointages déjà traités
    private long lastProcessedCount = 0;

    // Cache anti-doublons intra-appel
    private final Set<String> processedPointagesCache = Collections.synchronizedSet(new HashSet<>());

    private static final List<String> EXTERNAL_UID_KEYS = Arrays.asList(
            "external_uid", "externalUid", "uid", "device_id", "deviceId",
            "pastille_id", "pastilleId", "id", "badge_id", "badgeId"
    );

    private static final List<String> TIMESTAMP_KEYS = Arrays.asList(
            "event_time", "timestamp", "eventTime", "created_at", "date", "time", "scanned_at"
    );

    private static final List<String> TERMINAL_CODE_KEYS = Arrays.asList(
            "terminal_code", "terminalCode", "reader_id", "readerId", "terminal", "gateway_id"
    );

    private static final List<String> AGENT_CODE_KEYS = Arrays.asList(
            "agent_code", "agentCode", "user_id", "userId", "agent", "employee_id"
    );

    private static final List<String> SITE_CODE_KEYS = Arrays.asList(
            "site_code", "siteCode", "site_id", "siteId", "location_id"
    );

    @Scheduled(fixedDelay = 60000)
    public void pollFournisseurApi() {
        try {
            // 1. Initialisation du compteur
            if (lastProcessedCount == 0) {
                lastProcessedCount = factPointageRepository.count();
                log.info("[API] Initialisation - Pointages en base: {}", lastProcessedCount);
            }

            // 2. Toujours tout récupérer (J-7)
            String url = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/pointages")
                    .queryParam("from", LocalDateTime.now().minusDays(7).format(formatter))
                    .queryParam("limit", 1000)
                    .build()
                    .toUriString();

            // 3. Appel API
            FournisseurApiResponse response = callFournisseurApi(url);

            // 4. Traitement
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Object> allData = response.getData();

                if (!allData.isEmpty()) {
                    log.info("[API] {} pointages reçus du fournisseur", allData.size());

                    // 5. Filtrer pour ne garder que les nouveaux (après le compteur)
                    List<Object> newData = new ArrayList<>();
                    int index = 0;

                    for (Object item : allData) {
                        if (index >= lastProcessedCount) {
                            newData.add(item);
                        }
                        index++;
                    }

                    log.info("[API] {} nouveaux pointages identifiés (offset: {})", newData.size(), lastProcessedCount);

                    // 6. Envoyer les nouveaux
                    int envoyes = 0;

                    for (Object item : newData) {
                        try {
                            String externalUid = extractExternalUid(item);
                            String timestampStr = extractTimestamp(item);

                            if (externalUid == null || timestampStr == null) {
                                log.debug("Pointage ignoré - données incomplètes");
                                continue;
                            }

                            // Cache intra-appel uniquement
                            String uniqueKey = externalUid + "_" + timestampStr;
                            if (processedPointagesCache.contains(uniqueKey)) {
                                log.debug("Pointage ignoré - déjà dans le cache: {}", externalUid);
                                continue;
                            }
                            processedPointagesCache.add(uniqueKey);

                            String terminalCode = extractFirstString(item, TERMINAL_CODE_KEYS);
                            String agentCode = extractFirstString(item, AGENT_CODE_KEYS);
                            String siteCode = extractFirstString(item, SITE_CODE_KEYS);

                            WebSocketPointageDTO dto = new WebSocketPointageDTO();
                            dto.setExternalUid(externalUid);
                            dto.setTimestamp(timestampStr);
                            dto.setTerminalCode(terminalCode);
                            dto.setAgentCode(agentCode);
                            dto.setSiteCode(siteCode);
                            dto.setRawData(item.toString());

                            webSocketPointageService.receivePointage(dto);
                            envoyes++;

                        } catch (Exception e) {
                            log.error("Erreur traitement pointage: {}", e.getMessage());
                        }
                    }

                    // 7. Mettre à jour le compteur
                    lastProcessedCount += envoyes;
                    log.info("[API] {} nouveaux pointages envoyés. Total traité: {}", envoyes, lastProcessedCount);

                    // 8. Nettoyer le cache
                    if (processedPointagesCache.size() > 10000) {
                        processedPointagesCache.clear();
                        log.debug("[API] Cache vidé");
                    }

                } else {
                    log.info("[API] Aucun pointage reçu");
                }
            }

        } catch (Exception e) {
            log.error("[API] Erreur polling: {}", e.getMessage(), e);
        }
    }

    private FournisseurApiResponse callFournisseurApi(String url) {
        try {
            log.debug("[API] Appel: {}", url);

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
            log.error("[API] Échec appel: {}", e.getMessage());
            return null;
        }
    }

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

    private String extractFirstString(Object obj, List<String> keys) {
        if (!(obj instanceof Map)) return null;
        Map<?, ?> map = (Map<?, ?>) obj;
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

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