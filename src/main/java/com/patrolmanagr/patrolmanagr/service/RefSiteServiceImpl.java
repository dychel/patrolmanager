package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.Ref_siteDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefSiteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefSiteServiceImpl implements RefSiteService{

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    private RefSiteRepository refSiteRepository;
    @Autowired
    RefZoneService refZoneService;
    @Override
    public Ref_site saveSite(Ref_siteDTO ref_siteDTO) {
        Ref_site ref_site = modelMapper.map(ref_siteDTO, Ref_site.class);
        ref_site.setCreated_by(userService.getConnectedUserId());
        // Mettre à jour les clés étrangères
        updateForeignKeyZone(ref_siteDTO, ref_site);

        // Initialiser le statut si non fourni
        if (ref_site.getStatus() == null) {
            ref_site.setStatus(Status.ACTIVE);
        }
        return refSiteRepository.save(ref_site);
    }

    @Override
    public Ref_site updateSite(Long id, Ref_siteDTO ref_siteDTO) {
        Ref_site refSiteToUpdate = refSiteRepository.findByIdSite(id);
        if (refSiteToUpdate == null)
            throw new ApiRequestException("Site ID non trouvé");
        Ref_site ref_site = modelMapper.map(ref_siteDTO, Ref_site.class);
        ref_site.setId(ref_site.getId());
        ref_site.setUpdated_at(LocalDateTime.now());
        ref_site.setUpdated_by(userService.getConnectedUserId());
        // MAJ id zone
        updateForeignKeyZone(ref_siteDTO, ref_site);
        return refSiteRepository.save(ref_site);
    }

    private void updateForeignKeyZone(Ref_siteDTO ref_siteDTO, Ref_site ref_site) {
        // mettre a jour id Document si pas null
        if (ref_siteDTO.getZoneId() != null )
            ref_site.setRef_zone(refZoneService.findZoneById(ref_siteDTO.getZoneId()));
    }

    @Override
    public Ref_site findSiteById(Long id) {
        Ref_site siteToUpdate = refSiteRepository.findByIdSite(id);
        if (siteToUpdate == null)
            throw new ApiRequestException("Zone non trouvé");
        return siteToUpdate;
    }

    @Override
    public List<Ref_site> listSites() {
        List<Ref_site> listSite = refSiteRepository.findAll();
        if (listSite.isEmpty())
            throw new ApiRequestException("Pas de site enregister dans la base de donnees");
        return listSite;
    }

    @Override
    public Ref_site findSiteByIdZone(Long id) {
        Ref_zone ref_zone = refZoneService.findZoneById(id);
        if (ref_zone==null)
            throw new ApiRequestException("Pas de site enregister dans la base de donnees");
        return refSiteRepository.findSiteByZone(id);
    }

    @Override
    public void deleteSiteById(Long id) {
        Ref_site zoneToUpdate = refSiteRepository.findByIdSite(id);
        if (zoneToUpdate == null)
            throw new ApiRequestException("Site non trouvé");
        refSiteRepository.deleteById(id);
    }
}
