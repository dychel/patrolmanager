package com.stocktool.stocktool.controller;
import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/stocktool/produit/*")
public class ProduitController {

    @Autowired
    ProduitService produitService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllProduit(){
        return produitService.listProduits();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findProduitById(@PathVariable(value = "id") Long id){
        Produit produit = produitService.findProduitById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "produit", produit), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createProduit(@Valid @RequestBody ProduitDTO produitDTO){
        produitService.saveProduit(produitDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateProduit(@PathVariable(value = "id" ) Long id, @RequestBody ProduitDTO produitDTO){
        produitService.updateProduit(id, produitDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Produit Updated!", produitService.updateProduit(id, produitDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteProduit(@PathVariable(value = "id") Long id){
        produitService.deleteProduitById(id);
        return new ResponseEntity<>(new ResponseMessage("ok", "Produit" + id+ " deleted", null ), HttpStatus.OK);
    }
}
