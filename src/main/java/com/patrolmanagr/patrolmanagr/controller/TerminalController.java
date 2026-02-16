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
@RequestMapping("/api/v1/patrolmanagr/terminal")
public class TerminalController {

    @Autowired
    private RefTerminalService refTerminalService;

    @PostMapping("/add")
    public ResponseEntity<?> createTerminal(@RequestBody Ref_terminalDTO ref_terminalDTO) {
        try {
            Ref_terminal savedTerminal = refTerminalService.saveTerminal(ref_terminalDTO);
            return new ResponseEntity<>(
                    new ResponseMessage("ok", "Terminal " + ref_terminalDTO.getCode() + " créé avec succès", savedTerminal),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error", "Erreur lors de la création du terminal: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTerminals() {
        try {
            return new ResponseEntity<>(
                    new ResponseMessage("ok", "Liste des terminaux", refTerminalService.listTerminal()),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error", "Erreur lors de la récupération des terminaux: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/findbyid/{id}")
    public ResponseEntity<?> findTerminalById(@PathVariable Long id) {
        try {
            Ref_terminal refTerminal = refTerminalService.findTerminalById(id);
            return new ResponseEntity<>(
                    new ResponseMessage("ok", "Terminal trouvé", refTerminal),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error", "Terminal non trouvé avec l'ID: " + id, null),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTerminal(@PathVariable Long id) {
        try {
            refTerminalService.deleteTerminalById(id);
            return new ResponseEntity<>(
                    new ResponseMessage("ok", "Terminal supprimé avec succès", null),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseMessage("error", "Erreur lors de la suppression du terminal: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}