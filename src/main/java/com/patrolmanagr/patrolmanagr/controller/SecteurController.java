package com.patrolmanagr.patrolmanagr.controller;
import com.patrolmanagr.patrolmanagr.dto.Ref_secteurDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import com.patrolmanagr.patrolmanagr.response.ResponseMessage;
import com.patrolmanagr.patrolmanagr.service.RefSecteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping("/api/patrolmanagr/secteur/*")
public class SecteurController {

    @Autowired
    RefSecteurService refSecteurService;
    @PostMapping("/add")
    public ResponseEntity<?> createSecteur(@RequestBody Ref_secteurDTO ref_secteurDTO) {
        refSecteurService.saveSecteur(ref_secteurDTO);
        return new ResponseEntity<>(new ResponseMessage("ok", "secteur "+ ref_secteurDTO.getName()+ " Créé avec succès", ref_secteurDTO),
                HttpStatus.OK);
    }

    @GetMapping(value ="/all")
    public ResponseEntity<?> getAllSecteur() {
        return new ResponseEntity<>(new ResponseMessage("ok", "Liste des zone ", refSecteurService.listSecteurs()),
                HttpStatus.OK);
    }

    @GetMapping("findbyid/{id}")
    public ResponseEntity<ResponseMessage> findSecteuryId(@PathVariable(value = "id") Long id){
        Ref_secteur ref_secteur = refSecteurService.findSecteurById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "secteur trouvé", ref_secteur), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteSecteur(@PathVariable(value = "id") Long id) {
        refSecteurService.deleteSecteurById(id);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("delete", "secteur supprime avec succes"), HttpStatus.OK);
    }
}
