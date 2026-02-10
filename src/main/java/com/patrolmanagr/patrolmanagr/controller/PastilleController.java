package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.Ref_pastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefPastilleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/pastille")
public class PastilleController {

    @Autowired
    RefPastilleService refPastilleService;

    @PostMapping("/add")
    public ResponseEntity<ResponseMessage> createPastille(@Valid @RequestBody Ref_pastilleDTO refPastilleDTO) {
        try {
            Ref_pastille savedPastille = refPastilleService.savePastille(refPastilleDTO);
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Pastille " + savedPastille.getLabel() + " créée avec succès",
                    savedPastille
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllPastilles() {
        try {
            List<Ref_pastille> pastilles = refPastilleService.listRef_pastille();
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Liste des " + pastilles.size() + " pastilles",
                    pastilles
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseMessage> updatePastille(@PathVariable("id") Long id, @Valid @RequestBody Ref_pastilleDTO refPastilleDTO) {
        try {
            Ref_pastille updatedPastille = refPastilleService.updatePastille(id, refPastilleDTO);
            return new ResponseEntity<>(new ResponseMessage(
                    "update",
                    "Pastille mise à jour avec succès",
                    updatedPastille
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/findbyid/{id}")
    public ResponseEntity<ResponseMessage> findPastilleById(@PathVariable("id") Long id) {
        try {
            Ref_pastille pastille = refPastilleService.findPastilleById(id);
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Pastille trouvée",
                    pastille
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/findbyexternaluid/{external_uid}")
    public ResponseEntity<ResponseMessage> findPastilleByExternalUid(@PathVariable("external_uid") String external_uid) {
        try {
            Ref_pastille pastille = refPastilleService.findPastilleByExternalUid(external_uid);
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Pastille trouvée par external_uid",
                    pastille
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/findbycode/{code}")
    public ResponseEntity<ResponseMessage> findPastilleByCode(@PathVariable("code") String code) {
        try {
            Ref_pastille pastille = refPastilleService.findPastilleByCode(code);
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Pastille trouvée par code",
                    pastille
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/findbysite/{siteId}")
    public ResponseEntity<ResponseMessage> findPastillesBySite(@PathVariable("siteId") Long siteId) {
        try {
            List<Ref_pastille> pastilles = refPastilleService.findPastilleByIdSite(siteId);
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Liste des " + pastilles.size() + " pastilles pour le site ID " + siteId,
                    pastilles
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/findbysecteur/{secteurId}")
    public ResponseEntity<ResponseMessage> findPastillesBySecteur(@PathVariable("secteurId") Long secteurId) {
        try {
            List<Ref_pastille> pastilles = refPastilleService.findPastilleByIdSecteur(secteurId);
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Liste des " + pastilles.size() + " pastilles pour le secteur ID " + secteurId,
                    pastilles
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deletePastille(@PathVariable("id") Long id) {
        try {
            refPastilleService.deletePastilleById(id);
            return new ResponseEntity<>(new ResponseMessage(
                    "delete",
                    "Pastille supprimée avec succès",
                    null
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/findbyexternaluids")
    public ResponseEntity<ResponseMessage> findPastillesByExternalUids(@RequestBody List<String> externalUids) {
        try {
            List<Ref_pastille> pastilles = refPastilleService.findPastillesByExternalUids(externalUids);
            return new ResponseEntity<>(new ResponseMessage(
                    "ok",
                    "Trouvé " + pastilles.size() + " pastilles sur " + externalUids.size() + " recherchées",
                    pastilles
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(
                    "error",
                    e.getMessage(),
                    null
            ), HttpStatus.BAD_REQUEST);
        }
    }
}