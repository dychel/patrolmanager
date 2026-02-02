package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_siteDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import java.util.List;

public interface RefSiteService {
    Ref_site saveSite(Ref_siteDTO ref_siteDTO);
    Ref_site updateSite(Long id, Ref_siteDTO ref_siteDTO);
    Ref_site findSiteById(Long id);
    List<Ref_site> listSites();
    Ref_site findSiteByIdZone(Long id);
    Ref_site findSiteByIdClient(Long id);
    void deleteSiteById(Long id);
}
