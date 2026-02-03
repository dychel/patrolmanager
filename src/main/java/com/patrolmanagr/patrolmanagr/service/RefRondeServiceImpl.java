package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.Ref_rondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefRondeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefRondeServiceImpl implements RefRondeService {

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    RefRondeRepository refRondeRepository;
    @Autowired
    RefSiteService refSiteService;

    @Override
    public Ref_ronde saveRonde(Ref_rondeDTO ref_rondeDTO) {
        Ref_ronde ref_ronde = modelMapper.map(ref_rondeDTO, Ref_ronde.class);
        ref_ronde.setCreated_by(userService.getConnectedUserId());

        // Mettre à jour les clés étrangères
        updateForeignKeySite(ref_rondeDTO, ref_ronde);

        // Initialiser le statut si non fourni
        if (ref_ronde.getStatus() == null) {
            ref_ronde.setStatus(Status.ACTIVE);
        }

        //Ajout clientName et siteName.
        Ref_site ref_site = refSiteService.findSiteById(ref_rondeDTO.getSiteId());
        if (ref_site!=null){
            ref_ronde.setSiteName(ref_site.getName());
            ref_ronde.setClientName(ref_site.getClient_name());
        }

        // Initialiser les jours si non fourni
        if (ref_ronde.getJoursSemaine() == null || ref_ronde.getJoursSemaine().isEmpty()) {
            ref_ronde.setJoursSemaine("L,Ma,Me,J,V,S,D"); // Tous les jours par défaut
        }

        return refRondeRepository.save(ref_ronde);
    }

    @Override
    public Ref_ronde updateRonde(Long id, Ref_rondeDTO ref_rondeDTO) {
        Ref_ronde refRondeToUpdate = refRondeRepository.findByIdRonde(id);
        if (refRondeToUpdate == null)
            throw new ApiRequestException("Ronde ID non trouvé");

        Ref_ronde ref_ronde = modelMapper.map(ref_rondeDTO, Ref_ronde.class);
        ref_ronde.setId(id);
        ref_ronde.setUpdated_at(LocalDateTime.now());
        ref_ronde.setUpdated_by(userService.getConnectedUserId());

        // MAJ id site
        updateForeignKeySite(ref_rondeDTO, ref_ronde);

        // Conserver les champs non modifiés
        ref_ronde.setCreated_at(refRondeToUpdate.getCreated_at());
        ref_ronde.setCreated_by(refRondeToUpdate.getCreated_by());

        //Ajout clientName et siteName.
        Ref_site ref_site = refSiteService.findSiteById(ref_rondeDTO.getSiteId());
        if (ref_site!=null){
            ref_ronde.setSiteName(ref_site.getName());
            ref_ronde.setClientName(ref_site.getClient_name());
        }

        return refRondeRepository.save(ref_ronde);
    }

    private void updateForeignKeySite(Ref_rondeDTO ref_rondeDTO, Ref_ronde ref_ronde) {
        if (ref_rondeDTO.getSiteId() != null)
            ref_ronde.setRef_site(refSiteService.findSiteById(ref_rondeDTO.getSiteId()));
    }

    @Override
    public Ref_ronde findRondeById(Long id) {
        Ref_ronde rondeToUpdate = refRondeRepository.findByIdRonde(id);
        if (rondeToUpdate == null)
            throw new ApiRequestException("Ronde non trouvé");
        return rondeToUpdate;
    }

    @Override
    public List<Ref_ronde> listRonde() {
        List<Ref_ronde> ref_rondes = refRondeRepository.findAll();
        if (ref_rondes.isEmpty())
            throw new ApiRequestException("Pas de rondes enregistrées dans la base de données");
        return ref_rondes;
    }

    @Override
    public List<Ref_ronde> findRondeByIdSite(Long id) {
        Ref_site ref_site = refSiteService.findSiteById(id);
        if (ref_site == null)
            throw new ApiRequestException("Pas de site enregistré dans la base de données");
        return refRondeRepository.findByIdSite(id);
    }

    @Override
    public void deleteRondeById(Long id) {
        Ref_ronde ref_ronde = refRondeRepository.findByIdRonde(id);
        if (ref_ronde == null)
            throw new ApiRequestException("Ronde non trouvé");
        refRondeRepository.deleteById(id);
    }
}