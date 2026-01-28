package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_pastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefPastilleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/pastille/*")
public class PastilleController {

    @Autowired
    RefPastilleService refPastilleService;
    @PostMapping("/add")
    public ResponseEntity<?> createPastille(@RequestBody Ref_pastilleDTO refPastilleDTO) {
        refPastilleService.savePastille(refPastilleDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "Pastille "+ refPastilleDTO.getLabel()+ " Créé avec succès", refPastilleDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllPastille() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des Pastille ", refPastilleService.listRef_pastille()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findPastilleById(@PathVariable(value = "id") Long id){
        Ref_pastille ref_pastille = refPastilleService.findPastilleById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Pastille trouvé", ref_pastille), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deletePastille(@PathVariable(value = "id") Long id) {
        refPastilleService.deletePastilleById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "pastille supprime avec succes"), HttpStatus.OK);
    }
}


