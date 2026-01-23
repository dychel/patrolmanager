package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_secteurDTO;
import com.patrolmanagr.patrolmanagr.dto.Ref_siteDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefSecteurRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefSecteurServiceImpl implements RefSecteurService{

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    RefSecteurRepository refSecteurRepository;
    @Autowired
    RefSiteService refSiteService;
    @Override
    public Ref_secteur saveSecteur(Ref_secteurDTO ref_secteurDTO) {
        Ref_secteur ref_secteur = modelMapper.map(ref_secteurDTO, Ref_secteur.class);
        ref_secteur.setCreated_by(userService.getConnectedUserId());
        return refSecteurRepository.save(ref_secteur);
    }

    @Override
    public Ref_secteur updateSecteur(Long id, Ref_secteurDTO ref_secteurDTO) {
        Ref_secteur refsecteurToUpdate = refSecteurRepository.findByIdSecteur(id);
        if (refsecteurToUpdate == null)
            throw new ApiRequestException("Secteur ID non trouvé");
        Ref_secteur ref_secteur = modelMapper.map(ref_secteurDTO, Ref_secteur.class);
        ref_secteur.setId(ref_secteur.getId());
        ref_secteur.setUpdated_at(LocalDateTime.now());
        ref_secteur.setUpdated_by(userService.getConnectedUserId());
        // MAJ id zone
        updateForeignKeySite(ref_secteurDTO, ref_secteur);
        return refSecteurRepository.save(ref_secteur);
    }

    private void updateForeignKeySite(Ref_secteurDTO ref_secteurDTO, Ref_secteur ref_secteur) {
        // mettre a jour id Document si pas null
        if (ref_secteurDTO.getSiteId() != null )
            ref_secteur.setRef_site(refSiteService.findSiteById(ref_secteurDTO.getSiteId()));
    }

    @Override
    public Ref_secteur findSecteurById(Long id) {
        Ref_secteur secteurToUpdate = refSecteurRepository.findByIdSecteur(id);
        if (secteurToUpdate == null)
            throw new ApiRequestException("Zone non trouvé");
        return refSecteurRepository.findByIdSecteur(id);
    }

    @Override
    public List<Ref_secteur> listSecteurs() {
        List<Ref_secteur> listSecteur = refSecteurRepository.findAll();
        if (listSecteur.isEmpty())
            throw new ApiRequestException("Pas de secteur enregister dans la base de donnees");
        return listSecteur;
    }

    @Override
    public Ref_secteur findSecteurByIdSite(Long id) {
        Ref_site ref_site = refSiteService.findSiteById(id);
        if (ref_site==null)
            throw new ApiRequestException("Pas de site enregister dans la base de donnees");
        return refSecteurRepository.findSecteurBySite(id);
    }

    @Override
    public void deleteSecteurById(Long id) {
        Ref_secteur ref_secteur = refSecteurRepository.findByIdSecteur(id);
        if (ref_secteur == null)
            throw new ApiRequestException("Site non trouvé");
        refSecteurRepository.deleteById(id);
    }
}
