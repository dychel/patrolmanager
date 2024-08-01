package com.stocktool.stocktool.controller;
import com.stocktool.stocktool.dto.MenusDTO;
import com.stocktool.stocktool.entity.Menus;
import com.stocktool.stocktool.response.ResponseMessage;
import com.stocktool.stocktool.service.MenusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/stocktool/menus/*")
public class MenusController {

    @Autowired
    MenusService menusService;

    @GetMapping("/all")
    public ResponseEntity<ResponseMessage> getAllMenus(){
        return menusService.listMenus();
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findMenusById(@PathVariable(value = "id") Long id){
        Menus menus = menusService.findMenusById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Menu", menus), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createMenus(@Valid @RequestBody MenusDTO menusDTO){
        menusService.saveMenus(menusDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateMenus(@PathVariable(value = "id" ) Long id, @RequestBody MenusDTO menusDTO){
        menusService.updateMenus(id, menusDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Menu Updated!", menusService.updateMenus(id, menusDTO)), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteMenus(@PathVariable(value = "id") Long id){
        menusService.deleteMenusById(id);
        return new ResponseEntity<>(new ResponseMessage("ok", "Menu" + id+ " deleted", null ), HttpStatus.OK);
    }
}
