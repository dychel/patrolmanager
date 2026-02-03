package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ref_pastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import java.util.List;
import java.util.Map;

public interface RefPastilleService {
    Ref_pastille savePastille(Ref_pastilleDTO refPastilleDTO);
    Ref_pastille updatePastille(Long id, Ref_pastilleDTO refPastilleDTO);
    Ref_pastille findPastilleById(Long id);
    Ref_pastille findPastilleByExternalUid(String externalUid);
    List<Ref_pastille> findPastillesByExternalUids(List<String> externalUids);
    Ref_pastille findPastilleByCode(String code);
    List<Ref_pastille> listRef_pastille();
    List<Ref_pastille> findPastilleByIdSite(Long id);
    List<Ref_pastille> findPastilleByIdSecteur(Long id);
    void deletePastilleById(Long id);

    Map<String, Ref_pastille> getPastilleMapByExternalUids(List<String> externalUids);
}