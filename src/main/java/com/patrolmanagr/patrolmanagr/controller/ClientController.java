package com.patrolmanagr.patrolmanagr.controller;

import com.patrolmanagr.patrolmanagr.dto.Ref_clientDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_client;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/client/*")
public class ClientController {

    @Autowired
    RefClientService refClientService;

    @PostMapping("/add")
    public ResponseEntity<?> createClient(@RequestBody Ref_clientDTO ref_clientDTO) {
        refClientService.saveClient(ref_clientDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "Client " + ref_clientDTO.getName() + " créé avec succès", ref_clientDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllClients() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des clients", refClientService.listClients()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findClientById(@PathVariable(value = "id") Long id){
        Ref_client ref_client = refClientService.findClientById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Client trouvé", ref_client), HttpStatus.OK);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<ResponseMessage> updateClient(@PathVariable(value = "id") Long id,
                                                        @RequestBody Ref_clientDTO ref_clientDTO){
        Ref_client updatedClient = refClientService.updateClient(id, ref_clientDTO);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Client mis à jour avec succès", updatedClient), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable(value = "id") Long id) {
        refClientService.deleteClientById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "Client supprimé avec succès"), HttpStatus.OK);
    }
}