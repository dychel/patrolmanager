package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.WebSocketPointageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class PointageGeneratorService {

    @Autowired
    private WebSocketPointageService webSocketPointageService;

    private final Random random = new Random();
    private final List<String> TEST_UIDS = Arrays.asList(
            "56E7C660", "EXT-ABC-124", "EXT-ABC-325", "EXT-ABC-134", "EXT-ABC-440"
    );

    /**
     * AUTO-GÉNÈRE des pointages toutes les 30 secondes pour test
     */
    @Scheduled(fixedRate = 30000)
    public void generateTestPointages() {
        log.info("🎲 Génération pointages test...");

        int count = 1 + random.nextInt(3); // 1 à 3 pointages

        for (int i = 0; i < count; i++) {
            WebSocketPointageDTO pointage = new WebSocketPointageDTO();
            pointage.setExternalUid(TEST_UIDS.get(random.nextInt(TEST_UIDS.size())));
            pointage.setTerminalCode("TERM-" + (100 + random.nextInt(900)));
            pointage.setAgentCode("AGENT-" + (100 + random.nextInt(900)));
            pointage.setSiteCode(String.valueOf(1 + random.nextInt(3)));
            pointage.setTimestamp(LocalDateTime.now().toString());
            pointage.setRawData("{\"test\": true, \"rssi\": " + (-50 - random.nextInt(30)) + "}");

            webSocketPointageService.receivePointage(pointage);
            log.debug("Pointage test généré: {}", pointage.getExternalUid());
        }

        log.info("✅ {} pointage(s) test généré(s)", count);
    }
}