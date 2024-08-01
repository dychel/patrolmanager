package com.stocktool.stocktool.controller;

import com.stocktool.stocktool.dto.EquipeDTO;
import com.stocktool.stocktool.entity.Equipe;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.EquipeService;
import org.hibernate.boot.jaxb.Origin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/stocktool/equipe/*")
public class EquipeController {
    @Autowired
    EquipeService equipeService;

    @GetMapping("/all")
   public ResponseEntity<ResponseMessage> getAllEquipe(){
       return equipeService.listofequipe();
   }

   @GetMapping("findbyid/{id}")
   public ResponseEntity<ResponseMessage> findEquipeById(@PathVariable(value = "id") Long id){
       Equipe equipe = equipeService.findEquipeById(id);
       return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Equipe found", equipe),HttpStatus.OK);
   }
   @PostMapping("/add")
   public ResponseEntity<?> createEquipe(@Valid @RequestBody EquipeDTO equipeDTO){
        equipeService.saveEquipe(equipeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
   }

   @PutMapping(value = "/update/{id}")
   public ResponseEntity<?> updateEquipe(@PathVariable(value = "id" ) Long id, @RequestBody EquipeDTO equipeDTO){
        equipeService.updateEquipe(id, equipeDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Equipe Updated!",equipeService.updateEquipe(id, equipeDTO)), HttpStatus.OK);
   }

   @DeleteMapping(value = "/delete/{id}")
   public ResponseEntity<?> deleteEquipe(@PathVariable(value = "id") Long id){
        equipeService.deleteEquipeById(id);
        return new ResponseEntity<>(new ResponseMessage("ok", "contact" + id+ "deleted", null ), HttpStatus.OK);
   }
}
