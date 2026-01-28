package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.RefRondePastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde_pastille;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefRondePastilleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/ronde-pastille/*")
public class RondePastilleController {

    @Autowired
    private RefRondePastilleService refRondePastilleService;

    @PostMapping("/add")
    public ResponseEntity<?> createRondePastille(@RequestBody RefRondePastilleDTO refRondePastilleDTO) {
        refRondePastilleService.saveRondePastille(refRondePastilleDTO);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Association Ronde-Pastille créée avec succès", refRondePastilleDTO),
                HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllRondePastille() {
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des associations Ronde-Pastille", refRondePastilleService.listRondePastille()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findRondePastilleById(@PathVariable(value = "id") Long id) {
        Ref_ronde_pastille ref_ronde_pastille = refRondePastilleService.findRondePastilleById(id);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Association Ronde-Pastille trouvée", ref_ronde_pastille), HttpStatus.OK);
    }

    @GetMapping("findbyronde/{rondeId}")
    public ResponseEntity<ResponseMessage> findRondePastilleByRondeId(@PathVariable(value = "rondeId") Long rondeId) {
        List<Ref_ronde_pastille> rondePastilles = refRondePastilleService.findRondePastilleByRondeId(rondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastilles de la ronde trouvées", rondePastilles), HttpStatus.OK);
    }

    @GetMapping("findbypastille/{pastilleId}")
    public ResponseEntity<ResponseMessage> findRondePastilleByPastilleId(@PathVariable(value = "pastilleId") Long pastilleId) {
        List<Ref_ronde_pastille> rondePastilles = refRondePastilleService.findRondePastilleByPastilleId(pastilleId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Rondes associées à la pastille trouvées", rondePastilles), HttpStatus.OK);
    }

    @GetMapping("findbyrondeandsequence/{rondeId}/{sequence}")
    public ResponseEntity<ResponseMessage> findRondePastilleByRondeIdAndSequence(
            @PathVariable(value = "rondeId") Long rondeId,
            @PathVariable(value = "sequence") Integer sequence) {
        Ref_ronde_pastille rondePastille = refRondePastilleService.findRondePastilleByRondeIdAndSequence(rondeId, sequence);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Association Ronde-Pastille trouvée par séquence", rondePastille), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteRondePastille(@PathVariable(value = "id") Long id) {
        refRondePastilleService.deleteRondePastilleById(id);
        return new ResponseEntity<>(new ResponseMessage("delete",
                "Association Ronde-Pastille supprimée avec succès"), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete-by-ronde/{rondeId}")
    public ResponseEntity<?> deleteRondePastilleByRondeId(@PathVariable(value = "rondeId") Long rondeId) {
        refRondePastilleService.deleteRondePastilleByRondeId(rondeId);
        return new ResponseEntity<>(new ResponseMessage("delete",
                "Toutes les associations Ronde-Pastille de cette ronde ont été supprimées"), HttpStatus.OK);
    }
}