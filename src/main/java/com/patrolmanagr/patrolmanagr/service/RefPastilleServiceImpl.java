package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.Ref_pastilleDTO;
import com.patrolmanagr.patrolmanagr.dto.Ref_terminalDTO;
import com.patrolmanagr.patrolmanagr.entity.*;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefPastilleRepository;
import com.patrolmanagr.patrolmanagr.repository.RefSecteurRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefPastilleServiceImpl implements RefPastilleService {

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    RefPastilleRepository refPastilleRepository;
    @Autowired
    RefSiteService refSiteService;
    @Autowired
    RefSecteurService refSecteurService;

    @Override
    public Ref_pastille savePastille(Ref_pastilleDTO refPastilleDTO) {
        Ref_pastille ref_pastille = modelMapper.map(refPastilleDTO, Ref_pastille.class);
        ref_pastille.setCreated_by(userService.getConnectedUserId());
        // Mettre à jour les clés étrangères
        updateForeignKeySite_Secteur(refPastilleDTO, ref_pastille);

        // Initialiser le statut si non fourni
        if (ref_pastille.getStatus() == null) {
            ref_pastille.setStatus(Status.ACTIVE);
        }
        return refPastilleRepository.save(ref_pastille);
    }

    @Override
    public Ref_pastille updatePastille(Long id, Ref_pastilleDTO refPastilleDTO) {
        Ref_pastille refPastilleToUpdate = refPastilleRepository.findByIdPastille(id);
        if (refPastilleToUpdate == null)
            throw new ApiRequestException("Pastille ID non trouvé");
        Ref_pastille ref_pastille = modelMapper.map(refPastilleDTO, Ref_pastille.class);
        ref_pastille.setId(ref_pastille.getId());
        ref_pastille.setUpdated_at(LocalDateTime.now());
        ref_pastille.setUpdated_by(userService.getConnectedUserId());
        // MAJ id zone
        updateForeignKeySite_Secteur(refPastilleDTO, ref_pastille);
        return refPastilleRepository.save(ref_pastille);
    }

    private void updateForeignKeySite_Secteur(Ref_pastilleDTO refPastilleDTO, Ref_pastille ref_pastille) {
        // mettre a jour id Site si pas null
        if (refPastilleDTO.getRefSiteId() != null )
            ref_pastille.setRef_site_id(refSiteService.findSiteById(refPastilleDTO.getRefSiteId()));
        if (refPastilleDTO.getRefSecteurId() != null )
            ref_pastille.setRef_secteur_id(refSecteurService.findSecteurById(refPastilleDTO.getRefSecteurId()));
    }

    @Override
    public Ref_pastille findPastilleById(Long id) {
        Ref_pastille ref_pastille = refPastilleRepository.findByIdPastille(id);
        if (ref_pastille == null)
            throw new ApiRequestException("Pastille non trouvé");
        return refPastilleRepository.findByIdPastille(id);
    }

    @Override
    public List<Ref_pastille> listRef_pastille() {
        List<Ref_pastille> list = refPastilleRepository.findAll();
        if (list.isEmpty())
            throw new ApiRequestException("Pas de pastille enregister dans la base de donnees");
        return refPastilleRepository.findAll();
    }

    @Override
    public Ref_pastille findPastilleByIdSite(Long id) {
        Ref_site ref_site = refSiteService.findSiteById(id);
        if (ref_site==null)
            throw new ApiRequestException("Pas de pastille enregisté avec cet ID dans ce site");
        return refPastilleRepository.findByIdSite(id);
    }

    @Override
    public Ref_pastille findPastilleByIdSecteur(Long id) {
        Ref_secteur ref_secteur = refSecteurService.findSecteurById(id);
        if (ref_secteur==null)
            throw new ApiRequestException("Pas de pastille enregister dans ce secteur");
        return refPastilleRepository.findByIdSecteur(id);
    }

    @Override
    public void deletePastilleById(Long id) {
        Ref_pastille ref_pastille = refPastilleRepository.findByIdPastille(id);
        if (ref_pastille == null)
            throw new ApiRequestException("Pastille non trouvé");
        refPastilleRepository.deleteById(id);
    }
}
