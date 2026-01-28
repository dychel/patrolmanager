package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.dto.ExecRondeDTO;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.ExecRondeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/exec-ronde/*")
public class ExecRondeController {

    @Autowired
    ExecRondeService execRondeService;

    @PostMapping("/add")
    public ResponseEntity<?> createExecRonde(@RequestBody ExecRondeDTO execRondeDTO) {
        Exec_ronde savedExecRonde = execRondeService.saveExecRonde(execRondeDTO);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécution de ronde créée avec succès", savedExecRonde),
                HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllExecRonde() {
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des exécutions de ronde", execRondeService.listExecRonde()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findExecRondeById(@PathVariable(value = "id") Long id) {
        Exec_ronde exec_ronde = execRondeService.findExecRondeById(id);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécution de ronde trouvée", exec_ronde), HttpStatus.OK);
    }

    @GetMapping("findbyprogronde/{progRondeId}")
    public ResponseEntity<ResponseMessage> findExecRondeByProgRondeId(@PathVariable(value = "progRondeId") Long progRondeId) {
        List<Exec_ronde> execRondes = execRondeService.findExecRondeByProgRondeId(progRondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions de la programmation trouvées", execRondes), HttpStatus.OK);
    }

    @GetMapping("findbyrefronde/{refRondeId}")
    public ResponseEntity<ResponseMessage> findExecRondeByRefRondeId(@PathVariable(value = "refRondeId") Long refRondeId) {
        List<Exec_ronde> execRondes = execRondeService.findExecRondeByRefRondeId(refRondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions de la ronde trouvées", execRondes), HttpStatus.OK);
    }

    @GetMapping("findbysite/{siteId}")
    public ResponseEntity<ResponseMessage> findExecRondeBySiteId(@PathVariable(value = "siteId") Long siteId) {
        List<Exec_ronde> execRondes = execRondeService.findExecRondeBySiteId(siteId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions du site trouvées", execRondes), HttpStatus.OK);
    }

    @GetMapping("findbyexecdate/{execDate}")
    public ResponseEntity<ResponseMessage> findExecRondeByExecDate(
            @PathVariable(value = "execDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate execDate) {
        List<Exec_ronde> execRondes = execRondeService.findExecRondeByExecDate(execDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions de la date trouvées", execRondes), HttpStatus.OK);
    }

    @GetMapping("findbystatus/{status}")
    public ResponseEntity<ResponseMessage> findExecRondeByStatus(@PathVariable(value = "status") Status_exec_Ronde status) {
        List<Exec_ronde> execRondes = execRondeService.findExecRondeByStatus(status);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions avec ce statut trouvées", execRondes), HttpStatus.OK);
    }

    @GetMapping("findbysiteanddate/{siteId}/{execDate}")
    public ResponseEntity<ResponseMessage> findExecRondeBySiteIdAndExecDate(
            @PathVariable(value = "siteId") Long siteId,
            @PathVariable(value = "execDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate execDate) {
        List<Exec_ronde> execRondes = execRondeService.findExecRondeBySiteIdAndExecDate(siteId, execDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions du site et date trouvées", execRondes), HttpStatus.OK);
    }

    @GetMapping("findbyperiod")
    public ResponseEntity<ResponseMessage> findExecRondeByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Exec_ronde> execRondes = execRondeService.findExecRondeByPlannedStartAtBetween(startDate, endDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions dans la période trouvées", execRondes), HttpStatus.OK);
    }

    @PostMapping("/start/{id}")
    public ResponseEntity<ResponseMessage> startExecRonde(@PathVariable(value = "id") Long id) {
        Exec_ronde execRonde = execRondeService.startExecRonde(id);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécution de ronde démarrée avec succès", execRonde), HttpStatus.OK);
    }

    @PostMapping("/end/{id}")
    public ResponseEntity<ResponseMessage> endExecRonde(
            @PathVariable(value = "id") Long id,
            @RequestParam BigDecimal completionRate) {
        Exec_ronde execRonde = execRondeService.endExecRonde(id, completionRate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécution de ronde terminée avec succès", execRonde), HttpStatus.OK);
    }

    @PostMapping("/updatestatus/{id}")
    public ResponseEntity<ResponseMessage> updateExecRondeStatus(
            @PathVariable(value = "id") Long id,
            @RequestParam Status_exec_Ronde status,
            @RequestParam(required = false) BigDecimal completionRate) {
        Exec_ronde execRonde = execRondeService.updateExecRondeStatus(id, status, completionRate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Statut de l'exécution mis à jour avec succès", execRonde), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteExecRonde(@PathVariable(value = "id") Long id) {
        execRondeService.deleteExecRondeById(id);
        return new ResponseEntity<>(new ResponseMessage("delete",
                "Exécution de ronde supprimée avec succès"), HttpStatus.OK);
    }
}