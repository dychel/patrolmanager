package com.stocktool.stocktool.controller;
import com.stocktool.stocktool.dto.UniteDTO;
import com.stocktool.stocktool.entity.Unite;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.UniteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/unite/*")
public class UniteController {

    @Autowired
    UniteService uniteService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getUnite(){
        return uniteService.listofUnite();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findUniteById(@PathVariable(value = "id") Long id){
        Unite unite = uniteService.findUniteById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "list of unite", unite), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createUnite(@Valid @RequestBody UniteDTO uniteDTO){
        uniteService.saveUnite(uniteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUnite(@PathVariable(value = "id") Long id, @RequestBody UniteDTO uniteDTO){
        uniteService.updateUnite(id, uniteDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Unite updated", uniteService.updateUnite(id, uniteDTO)), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseMessage> deleteUnite(@PathVariable(value = "id") Long id){
        uniteService.deleteUniteById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "unite deleted", null), HttpStatus.OK);
    }

}
