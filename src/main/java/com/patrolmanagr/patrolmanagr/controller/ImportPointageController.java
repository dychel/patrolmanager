package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.ImportPointageService;
import com.patrolmanagr.patrolmanagr.service.ImportResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/pointages")
public class ImportPointageController {

    @Autowired
    private ImportPointageService importPointageService;

    @PostMapping("/import")
    public ResponseEntity<?> importPointages(
            @RequestParam("file") MultipartFile file) {

        try {
            // Vérifier le fichier
            if (file.isEmpty()) {
                return new ResponseEntity<>(
                        new ResponseMessage("error", "Fichier vide", null),
                        HttpStatus.BAD_REQUEST
                );
            }

            // Vérifier l'extension
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".csv") &&
                    !filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return new ResponseEntity<>(
                        new ResponseMessage("error", "Format non supporté. Utilisez CSV ou XLSX", null),
                        HttpStatus.BAD_REQUEST
                );
            }

            // Vérifier la taille (10MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                return new ResponseEntity<>(
                        new ResponseMessage("error", "Fichier trop volumineux. Maximum 10MB", null),
                        HttpStatus.BAD_REQUEST
                );
            }

            // Lancer l'import
            ImportResult result = importPointageService.importPointagesFromFile(file);

            if (result.isSuccess()) {
                return new ResponseEntity<>(
                        new ResponseMessage("ok", result.getMessage(), result),
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        new ResponseMessage("error", result.getMessage(), result),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error", "Erreur lors de l'import: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/import/template")
    public ResponseEntity<?> getImportTemplate() {
        String template = "date,heure,pastille,terminal,agent,site\n" +
                "2024-01-15,14:30:00,EXT_UID_123,TERM_001,AGENT_001,SITE_A\n" +
                "2024-01-15,15:45:00,EXT_UID_456,TERM_002,AGENT_002,SITE_B\n" +
                "2024-01-15,16:20:00,CODE_789,TERM_003,AGENT_003,SITE_C";

        return new ResponseEntity<>(
                new ResponseMessage("ok", "Template d'import CSV", template),
                HttpStatus.OK
        );
    }

    @GetMapping("/import/example")
    public ResponseEntity<?> getImportExample() {
        Map<String, Object> example = new HashMap<>();

        example.put("format", "CSV ou XLSX");
        example.put("encodage", "UTF-8");
        example.put("colonnes_requises", List.of("date", "heure", "pastille"));
        example.put("colonnes_optionnelles", List.of("terminal", "agent", "site"));

        Map<String, String> exempleLigne = new HashMap<>();
        exempleLigne.put("date", "2024-01-15");
        exempleLigne.put("heure", "14:30:00");
        exempleLigne.put("pastille", "EXT_UID_123 ou CODE_456");
        exempleLigne.put("terminal", "TERM_001 (optionnel)");
        exempleLigne.put("agent", "AGENT_001 (optionnel)");
        exempleLigne.put("site", "SITE_A (optionnel)");

        example.put("exemple_ligne", exempleLigne);
        example.put("note", "Les doublons sont autorisés");

        return new ResponseEntity<>(
                new ResponseMessage("ok", "Exemple d'import", example),
                HttpStatus.OK
        );
    }

    @GetMapping("/import/batch/{batchId}")
    public ResponseEntity<?> getBatchStatus(@PathVariable Long batchId) {
        // À implémenter pour récupérer les infos du batch
        return new ResponseEntity<>(
                new ResponseMessage("ok", "Statut du batch " + batchId, null),
                HttpStatus.OK
        );
    }
}