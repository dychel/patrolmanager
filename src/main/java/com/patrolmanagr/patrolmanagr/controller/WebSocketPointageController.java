package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.WebSocketPointageDTO;
import com.patrolmanagr.patrolmanagr.service.WebSocketPointageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebSocketPointageController {

    private final WebSocketPointageService webSocketPointageService;

    /**
     * ENDPOINT WEB SOCKET - Reçoit les pointages
     */
    @MessageMapping("/pointages/receive")
    @SendTo("/topic/pointages/ack")
    public Map<String, Object> receivePointage(WebSocketPointageDTO pointageDTO) {
        log.info("📡 WebSocket - Pointage reçu: {}", pointageDTO.getExternalUid());

        // Traiter le pointage
        webSocketPointageService.receivePointage(pointageDTO);

        // Retourner acknowledgement
        Map<String, Object> ack = new HashMap<>();
        ack.put("status", "OK");
        ack.put("externalUid", pointageDTO.getExternalUid());
        ack.put("timestamp", LocalDateTime.now().toString());
        ack.put("message", "Pointage reçu et en cours de traitement");

        return ack;
    }

    /**
     * ENDPOINT REST pour test (simule un terminal)
     */
    @PostMapping("/api/v1/pointages/add")
    public ResponseEntity<Map<String, Object>> simulatePointage(@RequestBody WebSocketPointageDTO pointageDTO) {
        log.info("🎯 Simulation pointage REST: {}", pointageDTO.getExternalUid());

        webSocketPointageService.receivePointage(pointageDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "received");
        response.put("externalUid", pointageDTO.getExternalUid());
        response.put("queueSize", webSocketPointageService.getQueueSize());
        response.put("message", "Pointage simulé envoyé au système");

        return ResponseEntity.ok(response);
    }

    /**
     * Monitoring WebSocket
     */
    @GetMapping("/api/v1/pointages/websocket-stats")
    public ResponseEntity<Map<String, Object>> getWebSocketStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReceived", webSocketPointageService.getTotalReceived());
        stats.put("totalProcessed", webSocketPointageService.getTotalProcessed());
        stats.put("queueSize", webSocketPointageService.getQueueSize());
        stats.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(stats);
    }

    /**
     * Test simple
     */
    @PostMapping("/api/v1/pointages/56E7C660")
    public ResponseEntity<Map<String, Object>> test56E7C660() {
        WebSocketPointageDTO pointage = new WebSocketPointageDTO();
        pointage.setExternalUid("56E7C660");
        pointage.setTerminalCode("RFID-TEST-001");
        pointage.setAgentCode("AGENT-TEST");
        pointage.setSiteCode("1");
        pointage.setTimestamp(LocalDateTime.now().toString());
        pointage.setRawData("{\"test\": true}");

        webSocketPointageService.receivePointage(pointage);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Pointage test 56E7C660 envoyé");
        response.put("externalUid", "56E7C660");

        return ResponseEntity.ok(response);
    }
}