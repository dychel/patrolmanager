package com.stocktool.stocktool.controller;

import com.stocktool.stocktool.dto.CategorieDTO;
import com.stocktool.stocktool.dto.MarqueDTO;
import com.stocktool.stocktool.entity.Categorie;
import com.stocktool.stocktool.entity.Marque;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.MarqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/marque/*")
public class MarqueController {

    @Autowired
    MarqueService marqueService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getMarque(){
        return marqueService.listofMarque();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findMarqueById(@PathVariable(value = "id") Long id){
        Marque marque = marqueService.findMarqueById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Marque", marque), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createMarque(@Valid @RequestBody MarqueDTO marqueDTO){
        marqueService.saveMarque(marqueDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMarque(@PathVariable(value = "id") Long id, @RequestBody MarqueDTO marqueDTO){
        marqueService.updateMarque(id, marqueDTO);
        Marque marque = marqueService.updateMarque(id, marqueDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "list", marque), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deleteMarque(@PathVariable(value = "id") Long id){
        marqueService.deleteMarqueById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "marque deleted", null), HttpStatus.OK);
    }
}
