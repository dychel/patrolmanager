package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_rondeDTO;
import com.patrolmanagr.patrolmanagr.dto.UserDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.User;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.response.UserResponse;
import com.patrolmanagr.patrolmanagr.service.RefRondeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/ronde/*")
public class RondeController {

    @Autowired
    RefRondeService refRondeSercice;
    @PostMapping("/add")
    public ResponseEntity<?> createRonde(@RequestBody Ref_rondeDTO ref_rondeDTO) {
        refRondeSercice.saveRonde(ref_rondeDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "ronde "+ ref_rondeDTO.getName()+ " Créé avec succès", ref_rondeDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllRonde() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des rondes ", refRondeSercice.listRonde()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findRondeById(@PathVariable(value = "id") Long id){
        Ref_ronde ref_ronde = refRondeSercice.findRondeById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "ronde trouvé", ref_ronde), HttpStatus.OK);
    }

    @GetMapping("findbysite/{siteId}")
    public ResponseEntity<ResponseMessage> findByRondeSiteId(@PathVariable(value = "siteId") Long siteId) {
        List<Ref_ronde> ref_rondes = refRondeSercice.findRondeByIdSite(siteId);
        return new ResponseEntity<>(new ResponseMessage("ok",
                "Liste des rondes pour le site Numero " + siteId, ref_rondes), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRonde(@PathVariable("id") Long id, @Valid @RequestBody Ref_rondeDTO ref_rondeDTO) {

        Ref_ronde rondeToUpdate = refRondeSercice.findRondeById(id);

        if (rondeToUpdate != null) {
            refRondeSercice.updateRonde(id, ref_rondeDTO);
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("update", "ronde mise a jour avec succes", ref_rondeDTO.getCode()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ResponseMessage("chao", "ronde non trouve !", null),
                    HttpStatus.OK);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteRonde(@PathVariable(value = "id") Long id) {
        refRondeSercice.deleteRondeById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "ronde supprimé avec succes"), HttpStatus.OK);
    }
}
