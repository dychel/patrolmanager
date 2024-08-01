package com.stocktool.stocktool.controller;
import com.stocktool.stocktool.dto.CompositionDTO;
import com.stocktool.stocktool.entity.Composition;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.CompositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/composition/*")
public class CompositionController {

    @Autowired
    CompositionService compositionService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllComposition(){
        return compositionService.listCompositions();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findCompositionById(@PathVariable(value = "id") Long id){
        Composition composition = compositionService.findCompositionById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "compo", composition), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createComposition(@Valid @RequestBody CompositionDTO compositionDTO){
        compositionService.saveComposition(compositionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateComposition(@PathVariable(value = "id" ) Long id, @RequestBody CompositionDTO compositionDTO){
        compositionService.updateComposition(id, compositionDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Compo Updated!", compositionService.updateComposition(id, compositionDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteComposition(@PathVariable(value = "id") Long id){
        compositionService.deleteCompositionById(id);
        return new ResponseEntity<>(new ResponseMessage("ok", "Compo" + id+ " deleted", null ), HttpStatus.OK);
    }

    @GetMapping("findbyprod/{id}")
    public List<Composition> findCompositionByIdProduit(@PathVariable(value = "id") Long id){
        return compositionService.getCompositionByProduit(id);
    }

    @GetMapping("findbymenu/{id}")
    public List<Composition> findCompositionByIdMenu(@PathVariable(value = "id") Long id){
        return compositionService.getCompositionByMenu(id);
    }

}
