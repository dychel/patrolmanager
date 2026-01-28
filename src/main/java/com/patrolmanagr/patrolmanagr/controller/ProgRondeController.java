package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.ProgRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Prog_ronde;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.ProgRondeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/prog-ronde/*")
public class ProgRondeController {

    @Autowired
    ProgRondeService progRondeService;

    @PostMapping("/add")
    public ResponseEntity<?> createProgRonde(@RequestBody ProgRondeDTO progRondeDTO) {
        progRondeService.saveProgRonde(progRondeDTO);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Programmation de ronde créée avec succès", progRondeDTO),
                HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllProgRonde() {
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des programmations de ronde", progRondeService.listProgRonde()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findProgRondeById(@PathVariable(value = "id") Long id) {
        Prog_ronde prog_ronde = progRondeService.findProgRondeById(id);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Programmation de ronde trouvée", prog_ronde), HttpStatus.OK);
    }

    @GetMapping("findbyronde/{rondeId}")
    public ResponseEntity<ResponseMessage> findProgRondeByRondeId(@PathVariable(value = "rondeId") Long rondeId) {
        List<Prog_ronde> progRondes = progRondeService.findProgRondeByRondeId(rondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Programmations de la ronde trouvées", progRondes), HttpStatus.OK);
    }

    @GetMapping("findbysite/{siteId}")
    public ResponseEntity<ResponseMessage> findProgRondeBySiteId(@PathVariable(value = "siteId") Long siteId) {
        List<Prog_ronde> progRondes = progRondeService.findProgRondeBySiteId(siteId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Programmations du site trouvées", progRondes), HttpStatus.OK);
    }

    @GetMapping("findbyuser/{userId}")
    public ResponseEntity<ResponseMessage> findProgRondeByUserId(@PathVariable(value = "userId") Long userId) {
        List<Prog_ronde> progRondes = progRondeService.findProgRondeByUserId(userId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Programmations de l'utilisateur trouvées", progRondes), HttpStatus.OK);
    }

    @GetMapping("findbystatus/{status}")
    public ResponseEntity<ResponseMessage> findProgRondeByStatus(@PathVariable(value = "status") Status status) {
        List<Prog_ronde> progRondes = progRondeService.findProgRondeByStatus(status);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Programmations avec ce statut trouvées", progRondes), HttpStatus.OK);
    }

    @GetMapping("findbyterminal/{terminalId}")
    public ResponseEntity<ResponseMessage> findProgRondeByTerminalId(@PathVariable(value = "terminalId") Long terminalId) {
        List<Prog_ronde> progRondes = progRondeService.findProgRondeByTerminalId(terminalId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Programmations du terminal trouvées", progRondes), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteProgRonde(@PathVariable(value = "id") Long id) {
        progRondeService.deleteProgRondeById(id);
        return new ResponseEntity<>(new ResponseMessage("delete",
                "Programmation de ronde supprimée avec succès"), HttpStatus.OK);
    }
}