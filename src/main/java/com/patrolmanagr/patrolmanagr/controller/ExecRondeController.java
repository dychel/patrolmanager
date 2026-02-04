package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.dto.ExecRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.ExecRondeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseEntity<?> createExecRonde(@Valid @RequestBody ExecRondeDTO execRondeDTO) {
        Exec_ronde exec_ronde = execRondeService.saveExecRonde(execRondeDTO);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécution de ronde créée avec succès", exec_ronde),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllExecRonde() {
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des exécutions de rondes", execRondeService.listExecRonde()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findExecRondeById(@PathVariable(value = "id") Long id){
        Exec_ronde exec_ronde = execRondeService.findExecRondeById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok",
                "Exécution de ronde trouvée", exec_ronde), HttpStatus.OK);
    }

    @GetMapping("findbyrefronde/{refRondeId}")
    public ResponseEntity<ResponseMessage> findExecRondeByRefRondeId(@PathVariable(value = "refRondeId") Long refRondeId) {
        List<Exec_ronde> exec_rondes = execRondeService.findExecRondeByRefRondeId(refRondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des exécutions pour la ronde " + refRondeId, exec_rondes), HttpStatus.OK);
    }

    @GetMapping("findbysite/{siteId}")
    public ResponseEntity<ResponseMessage> findExecRondeBySiteId(@PathVariable(value = "siteId") Long siteId) {
        List<Exec_ronde> exec_rondes = execRondeService.findExecRondeBySiteId(siteId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des exécutions pour le site " + siteId, exec_rondes), HttpStatus.OK);
    }

    @GetMapping("findbyexecdate/{execDate}")
    public ResponseEntity<ResponseMessage> findExecRondeByExecDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate execDate) {
        List<Exec_ronde> exec_rondes = execRondeService.findExecRondeByExecDate(execDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des exécutions pour la date " + execDate, exec_rondes), HttpStatus.OK);
    }

    @GetMapping("findbystatus/{status}")
    public ResponseEntity<ResponseMessage> findExecRondeByStatus(@PathVariable String status) {
        try {
            Status_exec_Ronde statusEnum = Status_exec_Ronde.valueOf(status.toUpperCase());
            List<Exec_ronde> exec_rondes = execRondeService.findExecRondeByStatus(statusEnum);
            return new ResponseEntity<>(new ResponseMessage("ok",
                    "Liste des exécutions avec statut " + status, exec_rondes), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage("error",
                    "Statut invalide. Valeurs acceptées: PLANNED, IN_PROGRESS, DONE, CANCELLED", null),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("findbysiteanddate/{siteId}/{execDate}")
    public ResponseEntity<ResponseMessage> findExecRondeBySiteIdAndExecDate(
            @PathVariable Long siteId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate execDate) {
        List<Exec_ronde> exec_rondes = execRondeService.findExecRondeBySiteIdAndExecDate(siteId, execDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions pour le site " + siteId + " à la date " + execDate, exec_rondes), HttpStatus.OK);
    }

    @GetMapping("findbyperiod")
    public ResponseEntity<ResponseMessage> findExecRondeByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Exec_ronde> exec_rondes = execRondeService.findExecRondeByPlannedStartAtBetween(startDate, endDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions dans la période spécifiée", exec_rondes), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateExecRonde(@PathVariable("id") Long id,
                                             @Valid @RequestBody ExecRondeDTO execRondeDTO) {
        Exec_ronde execRondeToUpdate = execRondeService.findExecRondeById(id);
        if (execRondeToUpdate != null) {
            Exec_ronde updated = execRondeService.updateExecRonde(id, execRondeDTO);
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("update",
                    "Exécution de ronde mise à jour avec succès", updated), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseMessage("error",
                    "Exécution de ronde non trouvée !", null), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteExecRonde(@PathVariable(value = "id") Long id) {
        execRondeService.deleteExecRondeById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete",
                "Exécution de ronde supprimée avec succès"), HttpStatus.OK);
    }

    @PutMapping("/start/{id}")
    public ResponseEntity<ResponseMessage> startExecRonde(@PathVariable Long id) {
        Exec_ronde exec_ronde = execRondeService.startExecRonde(id);
        return new ResponseEntity<>(new ResponseMessage("success",
                "Exécution de ronde démarrée", exec_ronde), HttpStatus.OK);
    }

    @PutMapping("/end/{id}")
    public ResponseEntity<ResponseMessage> endExecRonde(@PathVariable Long id,
                                                        @RequestParam BigDecimal completionRate) {
        Exec_ronde exec_ronde = execRondeService.endExecRonde(id, completionRate);
        return new ResponseEntity<>(new ResponseMessage("success",
                "Exécution de ronde terminée", exec_ronde), HttpStatus.OK);
    }

    @PutMapping("/updatestatus/{id}")
    public ResponseEntity<ResponseMessage> updateExecRondeStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) BigDecimal completionRate) {

        try {
            Status_exec_Ronde statusEnum = Status_exec_Ronde.valueOf(status.toUpperCase());
            Exec_ronde exec_ronde = execRondeService.updateExecRondeStatus(id, statusEnum, completionRate);
            return new ResponseEntity<>(new ResponseMessage("success",
                    "Statut de l'exécution mis à jour", exec_ronde), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage("error",
                    "Statut invalide. Valeurs acceptées: PLANNED, IN_PROGRESS, DONE, CANCELLED", null),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // NOUVELLES MÉTHODES AJOUTÉES

    @GetMapping("/findbyjobrun/{jobRunId}")
    public ResponseEntity<ResponseMessage> findExecRondeByJobRunId(@PathVariable Long jobRunId) {
        List<Exec_ronde> exec_rondes = execRondeService.findExecRondeByJobRunId(jobRunId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions pour le job run " + jobRunId, exec_rondes), HttpStatus.OK);
    }

    @GetMapping("/today/site/{siteId}")
    public ResponseEntity<ResponseMessage> findTodayExecRondesBySiteId(@PathVariable Long siteId) {
        List<Exec_ronde> exec_rondes = execRondeService.findTodayExecRondesBySiteId(siteId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions aujourd'hui pour le site " + siteId, exec_rondes), HttpStatus.OK);
    }

    @GetMapping("/stats/site/{siteId}")
    public ResponseEntity<ResponseMessage> getSiteExecRondeStats(
            @PathVariable Long siteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal avgCompletion = execRondeService.calculateAverageCompletionRate(siteId, startDate, endDate);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Statistiques d'exécution pour le site " + siteId, avgCompletion), HttpStatus.OK);
    }

    @GetMapping("/with-incidents")
    public ResponseEntity<ResponseMessage> findExecRondesWithIncidents() {
        List<Exec_ronde> exec_rondes = execRondeService.findExecRondesWithIncidents();
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Exécutions avec incidents (" + exec_rondes.size() + ")", exec_rondes), HttpStatus.OK);
    }

    @PostMapping("/create-from-ronde/{rondeId}")
    public ResponseEntity<ResponseMessage> createExecRondeFromRonde(
            @PathVariable Long rondeId,
            @RequestParam(required = false) Long jobRunId) {

        Exec_ronde exec_ronde = execRondeService.createExecRondeFromRonde(rondeId, jobRunId);
        return new ResponseEntity<>(new ResponseMessage("success",
                "Exécution de ronde créée à partir de la ronde " + rondeId, exec_ronde), HttpStatus.OK);
    }

    @GetMapping("/recent/{limit}")
    public ResponseEntity<ResponseMessage> findRecentExecRondes(@PathVariable int limit) {
        List<Exec_ronde> exec_rondes = execRondeService.findRecentExecRondes(limit);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Dernières " + limit + " exécutions de rondes", exec_rondes), HttpStatus.OK);
    }
}