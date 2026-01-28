package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_vendor_apiDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_vendor_api;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefVendorApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/vendorapi/*")
public class VendorApiController {

    @Autowired
    RefVendorApiService refVendorApiService;
    @PostMapping("/add")
    public ResponseEntity<?> createVendor(@RequestBody Ref_vendor_apiDTO refVendorApiDTO) {
        refVendorApiService.saveVendor(refVendorApiDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "vendor "+ refVendorApiDTO.getName()+ " Créé avec succès", refVendorApiDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllVendor() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des vendor api ", refVendorApiService.listVendor()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findVendorById(@PathVariable(value = "id") Long id){
        Ref_vendor_api ref_vendor_api = refVendorApiService.findVendorById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "zone trouvé", ref_vendor_api), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteVendor(@PathVariable(value = "id") Long id) {
        refVendorApiService.deleteVendorById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "vendor api supprime avec succes"), HttpStatus.OK);
    }
}
