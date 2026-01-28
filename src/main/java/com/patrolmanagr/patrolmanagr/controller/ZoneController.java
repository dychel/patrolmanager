package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_zoneDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/zone/*")
public class ZoneController {

    @Autowired
    RefZoneService refZoneService;
    @PostMapping("/add")
    public ResponseEntity<?> createZone(@RequestBody Ref_zoneDTO ref_zoneDTO) {
        refZoneService.saveZone(ref_zoneDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "zone "+ ref_zoneDTO.getName()+ " Créé avec succès", ref_zoneDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllZone() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des zone ", refZoneService.listZone()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findZoneById(@PathVariable(value = "id") Long id){
        Ref_zone ref_zone = refZoneService.findZoneById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "zone trouvé", ref_zone), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteZone(@PathVariable(value = "id") Long id) {
        refZoneService.deleteZoneById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "zone supprime avec succes"), HttpStatus.OK);
    }
}
