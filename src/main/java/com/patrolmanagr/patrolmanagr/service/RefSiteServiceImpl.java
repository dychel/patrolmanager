package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.Ref_siteDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import com.patrolmanagr.patrolmanagr.entity.Ref_client;
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

    @Autowired
    RefClientService refClientService;

    @Override
    public Ref_site saveSite(Ref_siteDTO ref_siteDTO) {
        Ref_site ref_site = modelMapper.map(ref_siteDTO, Ref_site.class);
        ref_site.setCreated_by(userService.getConnectedUserId());

        // Mettre à jour les clés étrangères
        updateForeignKeyZone(ref_siteDTO, ref_site);
        updateForeignKeyClient(ref_siteDTO, ref_site);

        // Initialiser le statut si non fourni
        if (ref_site.getStatus() == null) {
            ref_site.setStatus(Status.ACTIVE);
        }

        //client name
        Ref_client ref_client = refClientService.findClientById(ref_siteDTO.getClientId());
        if (ref_client != null){
            ref_site.setClient_name(ref_client.getName());
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
        ref_site.setClient_name(ref_siteDTO.getClient_name());

        // MAJ id zone et client
        updateForeignKeyZone(ref_siteDTO, ref_site);
        updateForeignKeyClient(ref_siteDTO, ref_site);

        return refSiteRepository.save(ref_site);
    }

    private void updateForeignKeyZone(Ref_siteDTO ref_siteDTO, Ref_site ref_site) {
        if (ref_siteDTO.getZoneId() != null)
            ref_site.setRef_zone(refZoneService.findZoneById(ref_siteDTO.getZoneId()));
    }

    private void updateForeignKeyClient(Ref_siteDTO ref_siteDTO, Ref_site ref_site) {
        if (ref_siteDTO.getClientId() != null)
            ref_site.setRef_client(refClientService.findClientById(ref_siteDTO.getClientId()));
    }

    @Override
    public Ref_site findSiteById(Long id) {
        Ref_site siteToUpdate = refSiteRepository.findByIdSite(id);
        if (siteToUpdate == null)
            throw new ApiRequestException("Site non trouvé");
        return siteToUpdate;
    }

    @Override
    public List<Ref_site> listSites() {
        List<Ref_site> listSite = refSiteRepository.findAll();
        if (listSite.isEmpty())
            throw new ApiRequestException("Pas de site enregistré dans la base de données");
        return listSite;
    }

    @Override
    public Ref_site findSiteByIdZone(Long id) {
        Ref_zone ref_zone = refZoneService.findZoneById(id);
        if (ref_zone == null)
            throw new ApiRequestException("Zone non trouvée");
        return refSiteRepository.findSiteByZone(id);
    }

    @Override
    public Ref_site findSiteByIdClient(Long id) {
        Ref_client ref_client = refClientService.findClientById(id);
        if (ref_client == null)
            throw new ApiRequestException("Client non trouvée");
        return refSiteRepository.findSiteByClient(id);
    }

    @Override
    public void deleteSiteById(Long id) {
        Ref_site siteToUpdate = refSiteRepository.findByIdSite(id);
        if (siteToUpdate == null)
            throw new ApiRequestException("Site non trouvé");
        refSiteRepository.deleteById(id);
    }
}