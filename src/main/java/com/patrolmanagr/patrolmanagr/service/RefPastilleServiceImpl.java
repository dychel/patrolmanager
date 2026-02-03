package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.Ref_pastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefPastilleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Transactional
    public Ref_pastille savePastille(Ref_pastilleDTO refPastilleDTO) {
        // Vérifier si l'external_uid existe déjà
        if (refPastilleDTO.getExternal_uid() != null) {
            Ref_pastille existing = refPastilleRepository.findByExternalUid(refPastilleDTO.getExternal_uid());
            if (existing != null) {
                throw new ApiRequestException("Une pastille avec cet external_uid existe déjà: " + refPastilleDTO.getExternal_uid());
            }
        }

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
    @Transactional
    public Ref_pastille updatePastille(Long id, Ref_pastilleDTO refPastilleDTO) {
        Ref_pastille refPastilleToUpdate = refPastilleRepository.findByIdPastille(id);
        if (refPastilleToUpdate == null)
            throw new ApiRequestException("Pastille ID non trouvé");

        // Vérifier la collision d'external_uid
        if (refPastilleDTO.getExternal_uid() != null &&
                !refPastilleDTO.getExternal_uid().equals(refPastilleToUpdate.getExternal_uid())) {
            Ref_pastille existing = refPastilleRepository.findByExternalUid(refPastilleDTO.getExternal_uid());
            if (existing != null && !existing.getId().equals(id)) {
                throw new ApiRequestException("Cet external_uid est déjà utilisé par une autre pastille");
            }
        }

        Ref_pastille ref_pastille = modelMapper.map(refPastilleDTO, Ref_pastille.class);
        ref_pastille.setId(id);
        ref_pastille.setUpdated_at(LocalDateTime.now());
        ref_pastille.setUpdated_by(userService.getConnectedUserId());

        // Conserver les champs non modifiables
        ref_pastille.setCreated_at(refPastilleToUpdate.getCreated_at());
        ref_pastille.setCreated_by(refPastilleToUpdate.getCreated_by());

        // MAJ des clés étrangères
        updateForeignKeySite_Secteur(refPastilleDTO, ref_pastille);

        return refPastilleRepository.save(ref_pastille);
    }

    private void updateForeignKeySite_Secteur(Ref_pastilleDTO refPastilleDTO, Ref_pastille ref_pastille) {
        // Mettre à jour id Site si pas null
        if (refPastilleDTO.getRefSiteId() != null) {
            Ref_site site = refSiteService.findSiteById(refPastilleDTO.getRefSiteId());
            ref_pastille.setRef_site_id(site);
        }

        // Mettre à jour id Secteur si pas null
        if (refPastilleDTO.getRefSecteurId() != null) {
            Ref_secteur secteur = refSecteurService.findSecteurById(refPastilleDTO.getRefSecteurId());
            ref_pastille.setRef_secteur_id(secteur);
        }
    }

    @Override
    @Cacheable(value = "pastilleById", key = "#id")
    public Ref_pastille findPastilleById(Long id) {
        Ref_pastille ref_pastille = refPastilleRepository.findByIdPastille(id);
        if (ref_pastille == null)
            throw new ApiRequestException("Pastille non trouvée avec ID: " + id);
        return ref_pastille;
    }

    @Override
    public Ref_pastille findPastilleByExternalUid(String external_uid) {
        Ref_pastille ref_pastille = refPastilleRepository.findByExternalUid(external_uid);

        if (external_uid == null || external_uid.trim().isEmpty()) {
            throw new ApiRequestException("External UID ne peut pas être vide");
        }
        if (ref_pastille == null) {
            throw new ApiRequestException("Pastille non trouvée avec external_uid: " + external_uid);
        }
        return ref_pastille;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ref_pastille> findPastillesByExternalUids(List<String> externalUids) {
        if (externalUids == null || externalUids.isEmpty()) {
            return List.of();
        }

        List<String> cleanedUids = externalUids.stream()
                .filter(uid -> uid != null && !uid.trim().isEmpty())
                .distinct()
                .toList();

        if (cleanedUids.isEmpty()) {
            return List.of();
        }

        return refPastilleRepository.findByExternalUidIn(cleanedUids);
    }

    @Override
    @Cacheable(value = "pastilleByCode", key = "#code")
    public Ref_pastille findPastilleByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ApiRequestException("Code ne peut pas être vide");
        }

        Ref_pastille ref_pastille = refPastilleRepository.findByCode(code);
        if (ref_pastille == null) {
            throw new ApiRequestException("Pastille non trouvée avec code: " + code);
        }
        return ref_pastille;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ref_pastille> listRef_pastille() {
        List<Ref_pastille> list = refPastilleRepository.findAll();
        if (list.isEmpty())
            throw new ApiRequestException("Pas de pastille enregistrée dans la base de données");
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ref_pastille> findPastilleByIdSite(Long id) {
        // Vérifier que le site existe
        Ref_site ref_site = refSiteService.findSiteById(id);
        if (ref_site == null) {
            throw new ApiRequestException("Site non trouvé avec ID: " + id);
        }

        List<Ref_pastille> pastilles = refPastilleRepository.findByIdSite(id);
        if (pastilles.isEmpty()) {
            throw new ApiRequestException("Pas de pastilles enregistrées pour ce site");
        }
        return pastilles;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ref_pastille> findPastilleByIdSecteur(Long id) {
        // Vérifier que le secteur existe
        Ref_secteur ref_secteur = refSecteurService.findSecteurById(id);
        if (ref_secteur == null) {
            throw new ApiRequestException("Secteur non trouvé avec ID: " + id);
        }

        List<Ref_pastille> pastilles = refPastilleRepository.findByIdSecteur(id);
        if (pastilles.isEmpty()) {
            throw new ApiRequestException("Pas de pastilles enregistrées pour ce secteur");
        }
        return pastilles;
    }

    @Override
    @Transactional
    public void deletePastilleById(Long id) {
        Ref_pastille ref_pastille = refPastilleRepository.findByIdPastille(id);
        if (ref_pastille == null)
            throw new ApiRequestException("Pastille non trouvée avec ID: " + id);

        // Vérifier si la pastille est utilisée dans des pointages
        // (À implémenter si vous avez une relation avec fact_pointage)

        refPastilleRepository.deleteById(id);
    }


    // MÉTHODE UTILITAIRE pour l'import batch
    public Map<String, Ref_pastille> getPastilleMapByExternalUids(List<String> externalUids) {
        List<Ref_pastille> pastilles = findPastillesByExternalUids(externalUids);
        return pastilles.stream()
                .filter(p -> p.getExternal_uid() != null)
                .collect(Collectors.toMap(
                        Ref_pastille::getExternal_uid,
                        p -> p,
                        (existing, replacement) -> existing
                ));
    }
}