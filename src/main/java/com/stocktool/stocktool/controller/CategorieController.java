package com.stocktool.stocktool.controller;
import com.stocktool.stocktool.dto.CategorieDTO;
import com.stocktool.stocktool.dto.EquipeDTO;
import com.stocktool.stocktool.entity.Categorie;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/categorie/*")
public class CategorieController {

    @Autowired
    CategorieService categorieService;
    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getCategorie(){
        return categorieService.listofCategorie();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findCategorieById(@PathVariable(value = "id") Long id){
        Categorie categorie = categorieService.findCategorieById(id);
       return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Categorie", categorie), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createCategorie(@Valid @RequestBody CategorieDTO categorieDTO){
        categorieService.saveCategorie(categorieDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCategorie(@PathVariable(value = "id") Long id, @RequestBody CategorieDTO categorieDTO){
        categorieService.updateCategorie(id, categorieDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "list", categorieService.updateCategorie(id, categorieDTO)), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deleteCategorie(@PathVariable(value = "id") Long id){
        categorieService.deleteCategorieById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Categorie deleted", null), HttpStatus.OK);
    }

}
