package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_terminalDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_terminal;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefTerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/v1/patrolmanagr/terminal/*")
public class TerminalController {

    @Autowired
    RefTerminalService refTerminalService;
    @PostMapping("/add")
    public ResponseEntity<?> createTerminal(@RequestBody Ref_terminalDTO ref_terminalDTO) {
        refTerminalService.saveTerminal(ref_terminalDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "terminal "+ ref_terminalDTO.getTerminalType()+ " Créé avec succès", ref_terminalDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllTerminal() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des terminaux ", refTerminalService.listTerminal()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findTerminalById(@PathVariable(value = "id") Long id){
        Ref_terminal ref_terminal = refTerminalService.findTerminalById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "terminal trouvé", ref_terminal), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteSite(@PathVariable(value = "id") Long id) {
        refTerminalService.deleteTerminalById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "terminal supprime avec succes"), HttpStatus.OK);
    }
}
