package com.stocktool.stocktool.controller;
import com.stocktool.stocktool.entity.DetailsVente;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.DetailsVenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/detailsvente/*")
public class DetailsVenteController {
    @Autowired
    DetailsVenteService detailsVenteService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllRavitaillement(){
        return detailsVenteService.listDetailsVentes();
    }
    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findDetailsVenteById(@PathVariable(value = "id") Long id){
        DetailsVente detailsVente = detailsVenteService.findDetailVenteById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Detail Vente " + id, detailsVente), HttpStatus.OK);
    }

    @GetMapping("findbyidprod/{id}")
    public ResponseEntity<?> findDetailsVenteByProduit(@PathVariable(value = "id") Long id){
        List<DetailsVente> detailsVente = detailsVenteService.getDetailVenteByProduit(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "details vente par produit "+id, detailsVente), HttpStatus.OK);
    }
    @GetMapping("findbyidmenu/{id}")
    public ResponseEntity<?> findDetailsVenteByMenu(@PathVariable(value = "id") Long id){
        List<DetailsVente> detailsVente = detailsVenteService.getDetailsVenteByMenu(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "details vente par menu "+id, detailsVente), HttpStatus.OK);
    }
    @GetMapping("findbyidvente/{id}")
    public ResponseEntity<?> findDetailsVenteByVente(@PathVariable(value = "id") Long id){
        List<DetailsVente> detailsVente = detailsVenteService.getDetailsVenteByVente(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "details vente par vente "+id, detailsVente), HttpStatus.OK);
    }
}
