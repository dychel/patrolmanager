package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_secteurDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import java.util.List;

public interface RefSecteurService {

    Ref_secteur saveSecteur(Ref_secteurDTO ref_secteurDTO);
    Ref_secteur updateSecteur(Long id, Ref_secteurDTO ref_secteurDTO);
    Ref_secteur findSecteurById(Long id);
    List<Ref_secteur> listSecteurs();
    Ref_secteur findSecteurByIdSite(Long id);
    void deleteSecteurById(Long id);
}
