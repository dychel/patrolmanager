package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.FactPointageDTO;
import com.patrolmanagr.patrolmanagr.dto.FactPointageImportDTO;
import com.patrolmanagr.patrolmanagr.entity.Fact_pointage;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.FactPointageService;
import com.patrolmanagr.patrolmanagr.service.PointageImportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/pointages")
public class FactPointageController {

    @Autowired
    private FactPointageService factPointageService;

    @Autowired
    private PointageImportService pointageImportService;

    @PostMapping("/add")
    public ResponseEntity<ResponseMessage> createPointage(@Valid @RequestBody FactPointageDTO factPointageDTO) {
        Fact_pointage pointage = factPointageService.savePointage(factPointageDTO);
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Pointage enregistré avec succès",
                pointage
        ), HttpStatus.CREATED);
    }

    @PostMapping("/import-batch")
    public ResponseEntity<ResponseMessage> importPointagesBatch(@RequestBody List<FactPointageImportDTO> importData) {
        if (importData == null || importData.isEmpty()) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    "Aucune donnée à importer",
                    null
            ), HttpStatus.BAD_REQUEST);
        }

        var future = pointageImportService.importPointagesBatch(importData);

        return new ResponseEntity<>(new ResponseMessage(
                "processing",
                "Import batch démarré pour " + importData.size() + " pointages",
                null
        ), HttpStatus.ACCEPTED);
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllPointages() {
        List<Fact_pointage> pointages = factPointageService.findAllPointages();
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Liste des " + pointages.size() + " pointages",
                pointages
        ), HttpStatus.OK);
    }

    @GetMapping("/findbyid/{id}")
    public ResponseEntity<ResponseMessage> findPointageById(@PathVariable("id") Long id) {
        Fact_pointage pointage = factPointageService.findPointageById(id);
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Pointage trouvé",
                pointage
        ), HttpStatus.OK);
    }

    @GetMapping("/findbyexternaluid/{externalUid}")
    public ResponseEntity<ResponseMessage> findPointagesByExternalUid(@PathVariable("externalUid") String externalUid) {
        List<Fact_pointage> pointages = factPointageService.findByExternalUid(externalUid);
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Trouvé " + pointages.size() + " pointages pour la pastille " + externalUid,
                pointages
        ), HttpStatus.OK);
    }

    @GetMapping("/findbysiteperiod/{siteId}")
    public ResponseEntity<ResponseMessage> findPointagesBySiteAndPeriod(
            @PathVariable("siteId") Long siteId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Fact_pointage> pointages = factPointageService.findBySiteAndPeriod(siteId, startDate, endDate);
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Trouvé " + pointages.size() + " pointages pour le site " + siteId,
                pointages
        ), HttpStatus.OK);
    }

    @GetMapping("/findbyrondeperiod/{rondeId}")
    public ResponseEntity<ResponseMessage> findPointagesByRondeAndPeriod(
            @PathVariable("rondeId") Long rondeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Fact_pointage> pointages = factPointageService.findByRondeAndPeriod(rondeId, startDate, endDate);
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Trouvé " + pointages.size() + " pointages pour la ronde " + rondeId,
                pointages
        ), HttpStatus.OK);
    }

    @GetMapping("/findbyagent/{agentId}")
    public ResponseEntity<ResponseMessage> findPointagesByAgent(@PathVariable("agentId") Long agentId) {
        List<Fact_pointage> pointages = factPointageService.findByAgent(agentId);
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                "Trouvé " + pointages.size() + " pointages pour l'agent " + agentId,
                pointages
        ), HttpStatus.OK);
    }

    @GetMapping("/pending")
    public ResponseEntity<ResponseMessage> getPendingPointages() {
        List<Fact_pointage> pointages = factPointageService.findPendingPointages();
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                pointages.size() + " pointages en attente de validation",
                pointages
        ), HttpStatus.OK);
    }

    @GetMapping("/rejected")
    public ResponseEntity<ResponseMessage> getRejectedPointages() {
        List<Fact_pointage> pointages = factPointageService.findRejectedPointages();
        return new ResponseEntity<>(new ResponseMessage(
                "ok",
                pointages.size() + " pointages rejetés",
                pointages
        ), HttpStatus.OK);
    }

    @PutMapping("/validate/{id}")
    public ResponseEntity<ResponseMessage> validatePointage(
            @PathVariable("id") Long id,
            @RequestParam(value = "notes", required = false) String validationNotes) {

        Fact_pointage pointage = factPointageService.validatePointage(id, validationNotes);
        return new ResponseEntity<>(new ResponseMessage(
                "validated",
                "Pointage validé avec succès",
                pointage
        ), HttpStatus.OK);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<ResponseMessage> rejectPointage(
            @PathVariable("id") Long id,
            @RequestParam("reason") String rejectionReason) {

        Fact_pointage pointage = factPointageService.rejectPointage(id, rejectionReason);
        return new ResponseEntity<>(new ResponseMessage(
                "rejected",
                "Pointage rejeté",
                pointage
        ), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseMessage> updatePointage(
            @PathVariable("id") Long id,
            @Valid @RequestBody FactPointageDTO factPointageDTO) {

        Fact_pointage pointage = factPointageService.updatePointage(id, factPointageDTO);
        return new ResponseEntity<>(new ResponseMessage(
                "update",
                "Pointage mis à jour avec succès",
                pointage
        ), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deletePointage(@PathVariable("id") Long id) {
        factPointageService.deletePointageById(id);
        return new ResponseEntity<>(new ResponseMessage(
                "delete",
                "Pointage supprimé avec succès",
                null
        ), HttpStatus.OK);
    }
}