package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_rondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefRondeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/patrolmanagr/ronde/*")
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

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteRonde(@PathVariable(value = "id") Long id) {
        refRondeSercice.deleteRondeById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "ronde supprimé avec succes"), HttpStatus.OK);
    }
}
