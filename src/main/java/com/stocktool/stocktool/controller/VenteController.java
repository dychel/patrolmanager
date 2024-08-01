package com.stocktool.stocktool.controller;

import com.stocktool.stocktool.dto.*;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.entity.Vente;
import com.stocktool.stocktool.repository.VenteRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.VenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/vente/*")
public class VenteController {

    @Autowired
    VenteService venteService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllVente(){
        return venteService.listVentes();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findVenteById(@PathVariable(value = "id") Long id){
        Vente vente = venteService.findVenteById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Vente", vente), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createVente(@Valid @RequestBody VenteDTO venteDTO, StockDTO stockDTO, DetailsVenteDTO detailsVenteDTO){
        venteService.saveVente(venteDTO, stockDTO, detailsVenteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateVente(@PathVariable(value = "id" ) Long id, @RequestBody VenteDTO venteDTO){
        venteService.updateVente(id, venteDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Vente Updated!", venteService.updateVente(id, venteDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteVente(@PathVariable(value = "id") Long id){
        venteService.deleteVenteById(id);
        return new ResponseEntity<>(new ResponseMessage("ok", "Vente " + id+ " deleted", null ), HttpStatus.OK);
    }
}
