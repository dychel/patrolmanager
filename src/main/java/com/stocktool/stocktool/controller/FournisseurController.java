package com.stocktool.stocktool.controller;

import com.stocktool.stocktool.dto.FournisseurDTO;
import com.stocktool.stocktool.entity.Fournisseur;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.FournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/fournisseur/*")
public class FournisseurController {
    @Autowired
    FournisseurService fournisseurService;
    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getFournisseur(){
        return fournisseurService.listofFournisseur();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findFournisseurById(@PathVariable(value = "id") Long id){
        Fournisseur fournisseur = fournisseurService.findFournisseurById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Fournisseur", fournisseur), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createFournisseur(@Valid @RequestBody FournisseurDTO fournisseurDTO){
        fournisseurService.saveFournisseur(fournisseurDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateFournisseur(@PathVariable(value = "id") Long id, @RequestBody FournisseurDTO fournisseurDTO){
        fournisseurService.updateFournisseur(id, fournisseurDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "list", fournisseurService.updateFournisseur(id, fournisseurDTO)), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deleteFournisseur(@PathVariable(value = "id") Long id){
        fournisseurService.deleteFournisseurById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Fourniseur deleted", null), HttpStatus.OK);
    }

}
