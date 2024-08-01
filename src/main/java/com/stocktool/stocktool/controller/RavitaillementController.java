package com.stocktool.stocktool.controller;

import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.dto.RavitaillementDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.RavitaillementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/stocktool/ravitaillement/*")
public class RavitaillementController {

    @Autowired
    RavitaillementService ravitaillementService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllRavitaillement(){
        return ravitaillementService.listRavitaillements();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findRavitaillementById(@PathVariable(value = "id") Long id){
        Ravitaillement ravitaillement = ravitaillementService.findRavitaillementById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "ravitaillement", ravitaillement), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createRavitallement(@Valid @RequestBody RavitaillementDTO ravitaillementDTO, StockDTO stockDTO){
        ravitaillementService.saveRavitaillement(ravitaillementDTO, stockDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateRavitaillement(@PathVariable(value = "id" ) Long id, @RequestBody RavitaillementDTO ravitaillementDTO){
        ravitaillementService.updateRavitaillement(id, ravitaillementDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "ravitaillement Updated!", ravitaillementService.updateRavitaillement(id, ravitaillementDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteRavitaillement(@PathVariable(value = "id") Long id){
        ravitaillementService.deleteRavitaillementById(id);
        return new ResponseEntity<>(new ResponseMessage("ok", "Ravitaillement " + id+ " deleted", null ), HttpStatus.OK);
    }
}
