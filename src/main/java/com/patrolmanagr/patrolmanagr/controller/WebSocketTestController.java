package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.service.WebSocketPointageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/websocket")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketPointageService webSocketPointageService;

    /**
     * Ajoute un pointage de test
     */
    @PostMapping("/add-test")
    public ResponseEntity<Map<String, Object>> addTestPointage(
            @RequestParam(defaultValue = "56E7C660") String externalUid) {

       // webSocketPointageService.addTestPointage(externalUid);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Pointage test ajouté");
        response.put("externalUid", externalUid);
        response.put("queueSize", webSocketPointageService.getQueueSize());
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Statut du service WebSocket
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", webSocketPointageService.getQueueSize());
        status.put("totalReceived", webSocketPointageService.getTotalReceived());
        status.put("totalProcessed", webSocketPointageService.getTotalProcessed());
        status.put("scheduledTask", "ACTIVE (chaque 1 minute)");
        status.put("lastCheck", LocalDateTime.now().toString());

        return ResponseEntity.ok(status);
    }

    /**
     * Force l'exécution du traitement
     */
    @PostMapping("/process-now")
    public ResponseEntity<Map<String, Object>> processNow() {
      //  webSocketPointageService.processBatchEveryMinute();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "processing");
        response.put("message", "Traitement batch déclenché manuellement");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}