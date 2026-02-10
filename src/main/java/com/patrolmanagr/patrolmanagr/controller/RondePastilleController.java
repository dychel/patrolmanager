package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.RefRondePastilleDTO;
import com.patrolmanagr.patrolmanagr.dto.RondePastilleOrderDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde_pastille;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefRondePastilleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PostMapping("/update-order")
    public ResponseEntity<?> updatePastilleOrder(@RequestBody RondePastilleOrderDTO orderDTO) {
        try {
            refRondePastilleService.updatePastilleOrder(orderDTO);
            return new ResponseEntity<>(new ResponseMessage("ok",
                    "Ordre des pastilles mis à jour avec succès", null),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage("error",
                    "Erreur lors de la mise à jour de l'ordre: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST);
        }
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

    @GetMapping("findbypastillebyronde/{rondeId}")
    public ResponseEntity<ResponseMessage> findRondePastilleByRondeId(@PathVariable(value = "rondeId") Long rondeId) {
        List<Ref_ronde_pastille> rondePastilles = refRondePastilleService.findRondePastilleByRondeId(rondeId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Pastilles de la ronde trouvées", rondePastilles), HttpStatus.OK);
    }

    @GetMapping("details-by-ronde/{rondeId}")
    public ResponseEntity<ResponseMessage> getPastilleDetailsForRonde(@PathVariable Long rondeId) {
        try {
            List<Ref_ronde_pastille> pastilles = refRondePastilleService.getPastillesForRondeWithDetails(rondeId);

            // Transformer en format simplifié pour le frontend
            List<Map<String, Object>> result = pastilles.stream().map(assoc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", assoc.getId());
                map.put("pastilleId", assoc.getRef_pastille_id().getId());
                map.put("label", assoc.getRef_pastille_id().getLabel());
                map.put("code", assoc.getRef_pastille_id().getCode());
                map.put("externalUid", assoc.getRef_pastille_id().getExternal_uid());
                map.put("seq_no", assoc.getSeq_no());
                map.put("secteur", assoc.getRef_pastille_id().getRef_secteur_id() != null
                        ? assoc.getRef_pastille_id().getRef_secteur_id().getName()
                        : "Non assigné");
                map.put("technologie", getTechnologieFromExternalUid(assoc.getRef_pastille_id().getExternal_uid()));
                return map;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(new ResponseMessage("ok",
                    "Pastilles de la ronde avec détails", result),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage("error",
                    "Erreur lors de la récupération: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST);
        }
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

    private String getTechnologieFromExternalUid(String externalUid) {
        if (externalUid == null) return "NFC";
        if (externalUid.toUpperCase().contains("RFID")) return "RFID";
        if (externalUid.toUpperCase().contains("NFC")) return "NFC";
        if (externalUid.toUpperCase().contains("QR")) return "QR Code";
        return "NFC";
    }
}