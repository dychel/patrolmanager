package com.stocktool.stocktool.controller;

import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.dto.RavitaillementDTO;
import com.stocktool.stocktool.entity.Ravitaillement;
import com.stocktool.stocktool.entity.Stock;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/stocktool/stock/*")
public class StockController {

    @Autowired
    StockService stockService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllStock(){
        return stockService.listStock();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findStockById(@PathVariable(value = "id") Long id){
        Stock stock = stockService.findStockById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "stock", stock), HttpStatus.OK);
    }

    @GetMapping("findbyidprod/{id}")
    public ResponseEntity<?> findStockByProduit(@PathVariable(value = "id") Long id){
        Stock stock = stockService.findStockByProduit(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "stock", stock), HttpStatus.OK);
    }

}
