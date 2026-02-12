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
     * ENDPOINT WEBSOCKET - Réception des pointages
     * Utilisé par l'API fournisseur en temps réel
     */
    @MessageMapping("/pointages/receive")
    @SendTo("/topic/pointages/ack")
    public Map<String, Object> receivePointage(WebSocketPointageDTO pointageDTO) {
        log.info("📡 [WebSocket] Pointage reçu: externalUid={}", pointageDTO.getExternalUid());

        // Traiter le pointage
        webSocketPointageService.receivePointage(pointageDTO);

        // Accusé de réception
        Map<String, Object> ack = new HashMap<>();
        ack.put("status", "OK");
        ack.put("code", "POINTAGE_RECEIVED");
        ack.put("externalUid", pointageDTO.getExternalUid());
        ack.put("timestamp", LocalDateTime.now().toString());
        ack.put("queueSize", webSocketPointageService.getQueueSize());
        ack.put("message", "Pointage reçu et en attente de traitement");

        return ack;
    }

    /**
     * ENDPOINT REST - Pour tests manuels
     * Simule l'envoi d'un pointage depuis l'API fournisseur
     */
    @PostMapping("/api/v1/fournisseur/pointages")
    public ResponseEntity<Map<String, Object>> receiveFromFournisseur(@RequestBody WebSocketPointageDTO pointageDTO) {
        log.info("📨 [REST] Pointage reçu depuis simulateur: {}", pointageDTO.getExternalUid());

        webSocketPointageService.receivePointage(pointageDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("externalUid", pointageDTO.getExternalUid());
        response.put("queueSize", webSocketPointageService.getQueueSize());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Pointage envoyé au traitement");

        return ResponseEntity.ok(response);
    }

    /**
     * ENDPOINT STATISTIQUES - Monitoring
     */
    @GetMapping("/api/v1/pointages/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = webSocketPointageService.getStats();
        stats.put("service", "API_FOURNISSEUR");
        stats.put("endpoint", "/api/v1/fournisseur/pointages");
        return ResponseEntity.ok(stats);
    }

    /**
     * ENDPOINT SANTÉ - Vérification du service
     */
    @GetMapping("/api/v1/pointages/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "FournisseurApiService");
        health.put("queueSize", webSocketPointageService.getQueueSize());
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}