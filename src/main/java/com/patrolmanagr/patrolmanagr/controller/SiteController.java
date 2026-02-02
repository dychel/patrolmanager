package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_siteDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/site/*")
public class SiteController {

    @Autowired
    RefSiteService refSiteService;
    @PostMapping("/add")
    public ResponseEntity<?> createSite(@RequestBody Ref_siteDTO ref_siteDTO) {
        refSiteService.saveSite(ref_siteDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "site "+ ref_siteDTO.getName()+ " Créé avec succès", ref_siteDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllSite() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des zone ", refSiteService.listSites()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findSiteById(@PathVariable(value = "id") Long id){
        Ref_site ref_site = refSiteService.findSiteById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "site trouvé", ref_site), HttpStatus.OK);
    }

    @GetMapping("findbyidzone/{zoneId}")
    public ResponseEntity<ResponseMessage> findSiteByIdZone(@PathVariable(value = "zoneId") Long zoneId){
        Ref_site ref_site = refSiteService.findSiteByIdZone(zoneId);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "site trouvé par zone", ref_site), HttpStatus.OK);
    }


    @GetMapping("findbyclient/{zoneId}")
    public ResponseEntity<ResponseMessage> findSiteByIdClient(@PathVariable(value = "clientId") Long clientId){
        Ref_site ref_site = refSiteService.findSiteByIdClient(clientId);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Site trouve pour ce client", ref_site), HttpStatus.OK);
    }


    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteSite(@PathVariable(value = "id") Long id) {
        refSiteService.deleteSiteById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "site supprime avec succes"), HttpStatus.OK);
    }
}
