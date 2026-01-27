package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.ExecRondePastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.exec_ronde_pastille;
import com.patrolmanagr.patrolmanagr.config.Status_ronde_pastille;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.ExecRondePastilleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/patrolmanagr/exec-ronde-pastille/*")
public class ExecRondePastilleController {

    @Autowired
    ExecRondePastilleService execRondePastilleService;

    @PostMapping("/add")
    public ResponseEntity<?> createExecRondePastille(@RequestBody ExecRondePastilleDTO execRondePastilleDTO) {
        exec_ronde_pastille savedExecRondePastille = execRondePastilleService.saveExecRondePastille(execRondePastilleDTO);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécution de pastille créée avec succès", savedExecRondePastille),
                HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllExecRondePastille() {
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des exécutions de pastilles", execRondePastilleService.listExecRondePastille()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findExecRondePastilleById(@PathVariable(value = "id") Long id) {
        exec_ronde_pastille exec_ronde_pastille = execRondePastilleService.findExecRondePastilleById(id);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécution de pastille trouvée", exec_ronde_pastille), HttpStatus.OK);
    }

    @GetMapping("findbyexecronde/{execRondeId}")
    public ResponseEntity<ResponseMessage> findExecRondePastilleByExecRondeId(@PathVariable(value = "execRondeId") Long execRondeId) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleService.findExecRondePastilleByExecRondeId(execRondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastilles de l'exécution trouvées", execRondePastilles), HttpStatus.OK);
    }

    @GetMapping("findbyexecrondeordered/{execRondeId}")
    public ResponseEntity<ResponseMessage> findExecRondePastilleByExecRondeIdOrdered(@PathVariable(value = "execRondeId") Long execRondeId) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleService.findExecRondePastilleByExecRondeIdOrderBySeqNo(execRondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastilles de l'exécution triées par séquence", execRondePastilles), HttpStatus.OK);
    }

    @GetMapping("findbypastille/{pastilleId}")
    public ResponseEntity<ResponseMessage> findExecRondePastilleByPastilleId(@PathVariable(value = "pastilleId") Long pastilleId) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleService.findExecRondePastilleByPastilleId(pastilleId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions de la pastille trouvées", execRondePastilles), HttpStatus.OK);
    }

    @GetMapping("findbyexecrondeandseq/{execRondeId}/{seqNo}")
    public ResponseEntity<ResponseMessage> findExecRondePastilleByExecRondeIdAndSeqNo(
            @PathVariable(value = "execRondeId") Long execRondeId,
            @PathVariable(value = "seqNo") Integer seqNo) {
        exec_ronde_pastille execRondePastille = execRondePastilleService.findExecRondePastilleByExecRondeIdAndSeqNo(execRondeId, seqNo);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastille de l'exécution trouvée par séquence", execRondePastille), HttpStatus.OK);
    }

    @GetMapping("findbystatus/{status}")
    public ResponseEntity<ResponseMessage> findExecRondePastilleByStatus(@PathVariable(value = "status") Status_ronde_pastille status) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleService.findExecRondePastilleByStatus(status);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions de pastilles avec ce statut trouvées", execRondePastilles), HttpStatus.OK);
    }

    @GetMapping("findbyexecrondeandstatus/{execRondeId}/{status}")
    public ResponseEntity<ResponseMessage> findExecRondePastilleByExecRondeIdAndStatus(
            @PathVariable(value = "execRondeId") Long execRondeId,
            @PathVariable(value = "status") Status_ronde_pastille status) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleService.findExecRondePastilleByExecRondeIdAndStatus(execRondeId, status);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastilles de l'exécution avec ce statut trouvées", execRondePastilles), HttpStatus.OK);
    }

    @GetMapping("findbyscannedperiod")
    public ResponseEntity<ResponseMessage> findExecRondePastilleByScannedAtBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleService.findExecRondePastilleByScannedAtBetween(startDate, endDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions de pastilles scannées dans la période", execRondePastilles), HttpStatus.OK);
    }

    @PostMapping("/markasdone/{id}")
    public ResponseEntity<ResponseMessage> markAsDone(
            @PathVariable(value = "id") Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scannedAt,
            @RequestParam(required = false) Integer actualTravelSec,
            @RequestParam(required = false) String notes) {
        exec_ronde_pastille execRondePastille = execRondePastilleService.markAsDone(id, scannedAt, actualTravelSec, notes);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastille marquée comme DONE avec succès", execRondePastille), HttpStatus.OK);
    }

    @PostMapping("/markasmissed/{id}")
    public ResponseEntity<ResponseMessage> markAsMissed(
            @PathVariable(value = "id") Long id,
            @RequestParam(required = false) String notes) {
        exec_ronde_pastille execRondePastille = execRondePastilleService.markAsMissed(id, notes);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastille marquée comme MISSED avec succès", execRondePastille), HttpStatus.OK);
    }

    @PostMapping("/updatepointage/{id}")
    public ResponseEntity<ResponseMessage> updatePointage(
            @PathVariable(value = "id") Long id,
            @RequestParam Long pointageId) {
        exec_ronde_pastille execRondePastille = execRondePastilleService.updatePointage(id, pointageId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pointage mis à jour avec succès", execRondePastille), HttpStatus.OK);
    }

    @PostMapping("/initialize/{execRondeId}")
    public ResponseEntity<ResponseMessage> initializeFromRonde(@PathVariable(value = "execRondeId") Long execRondeId) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleService.initializeFromRonde(execRondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastilles initialisées à partir de la ronde avec succès", execRondePastilles), HttpStatus.OK);
    }

    @GetMapping("/count/{execRondeId}/{status}")
    public ResponseEntity<ResponseMessage> countByExecRondeIdAndStatus(
            @PathVariable(value = "execRondeId") Long execRondeId,
            @PathVariable(value = "status") Status_ronde_pastille status) {
        Long count = execRondePastilleService.countByExecRondeIdAndStatus(execRondeId, status);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Nombre de pastilles avec ce statut", count), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteExecRondePastille(@PathVariable(value = "id") Long id) {
        execRondePastilleService.deleteExecRondePastilleById(id);
        return new ResponseEntity<>(new ResponseMessage("delete",
                "Exécution de pastille supprimée avec succès"), HttpStatus.OK);
    }
}