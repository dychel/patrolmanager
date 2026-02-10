package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.Ref_pastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_secteur;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefPastilleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RefPastilleServiceImpl implements RefPastilleService {

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
        // Validation des données obligatoires
        if (refPastilleDTO.getCode() == null || refPastilleDTO.getCode().trim().isEmpty()) {
            throw new ApiRequestException("Le code de la pastille est obligatoire");
        }

        if (refPastilleDTO.getLabel() == null || refPastilleDTO.getLabel().trim().isEmpty()) {
            throw new ApiRequestException("Le label de la pastille est obligatoire");
        }

        if (refPastilleDTO.getRefSiteId() == null) {
            throw new ApiRequestException("Le site est obligatoire");
        }

        // Vérifier si le code existe déjà
        Ref_pastille existingByCode = refPastilleRepository.findByCode(refPastilleDTO.getCode());
        if (existingByCode != null) {
            throw new ApiRequestException("Une pastille avec ce code existe déjà: " + refPastilleDTO.getCode());
        }

        // Vérifier si l'external_uid existe déjà (si fourni)
        if (refPastilleDTO.getExternal_uid() != null && !refPastilleDTO.getExternal_uid().trim().isEmpty()) {
            Ref_pastille existingByUid = refPastilleRepository.findByExternalUid(refPastilleDTO.getExternal_uid());
            if (existingByUid != null) {
                throw new ApiRequestException("Une pastille avec cet external_uid existe déjà: " + refPastilleDTO.getExternal_uid());
            }
        }

        // Créer une nouvelle pastille
        Ref_pastille ref_pastille = new Ref_pastille();
        ref_pastille.setCode(refPastilleDTO.getCode().trim());
        ref_pastille.setLabel(refPastilleDTO.getLabel().trim());

        // Gérer external_uid
        if (refPastilleDTO.getExternal_uid() != null && !refPastilleDTO.getExternal_uid().trim().isEmpty()) {
            ref_pastille.setExternal_uid(refPastilleDTO.getExternal_uid().trim());
        }

        // Gérer temps théorique
        ref_pastille.setTempsTheorique(refPastilleDTO.getTempsTheorique());

        // Gérer audit field
        ref_pastille.setAudit_field(refPastilleDTO.getAudit_field());

        // Définir l'utilisateur connecté comme créateur
        Long connectedUserId = userService.getConnectedUserId();
        ref_pastille.setCreated_by(connectedUserId);
        ref_pastille.setCreated_at(LocalDateTime.now());

        // Mettre à jour les clés étrangères
        updateForeignKeySite_Secteur(refPastilleDTO, ref_pastille);

        // Initialiser le statut si non fourni
        if (refPastilleDTO.getStatus() != null) {
            ref_pastille.setStatus(refPastilleDTO.getStatus());
        } else {
            ref_pastille.setStatus(Status.ACTIVE);
        }

        // Initialiser les autres champs
        ref_pastille.setIs_deleted(false);

        return refPastilleRepository.save(ref_pastille);
    }

    @Override
    @Transactional
    public Ref_pastille updatePastille(Long id, Ref_pastilleDTO refPastilleDTO) {
        // Trouver la pastille existante
        Optional<Ref_pastille> optionalPastille = refPastilleRepository.findById(id);
        if (optionalPastille.isEmpty()) {
            throw new ApiRequestException("Pastille non trouvée avec ID: " + id);
        }

        Ref_pastille existingPastille = optionalPastille.get();

        // Validation des données obligatoires
        if (refPastilleDTO.getCode() == null || refPastilleDTO.getCode().trim().isEmpty()) {
            throw new ApiRequestException("Le code de la pastille est obligatoire");
        }

        if (refPastilleDTO.getLabel() == null || refPastilleDTO.getLabel().trim().isEmpty()) {
            throw new ApiRequestException("Le label de la pastille est obligatoire");
        }

        if (refPastilleDTO.getRefSiteId() == null) {
            throw new ApiRequestException("Le site est obligatoire");
        }

        // Vérifier la collision de code (si modifié)
        if (!refPastilleDTO.getCode().equals(existingPastille.getCode())) {
            Ref_pastille existingByCode = refPastilleRepository.findByCode(refPastilleDTO.getCode());
            if (existingByCode != null && !existingByCode.getId().equals(id)) {
                throw new ApiRequestException("Ce code est déjà utilisé par une autre pastille: " + refPastilleDTO.getCode());
            }
        }

        // Vérifier la collision d'external_uid (si modifié)
        String newExternalUid = refPastilleDTO.getExternal_uid();
        String currentExternalUid = existingPastille.getExternal_uid();

        if (newExternalUid != null && !newExternalUid.trim().isEmpty()) {
            if (currentExternalUid == null || !currentExternalUid.equals(newExternalUid.trim())) {
                Ref_pastille existingByUid = refPastilleRepository.findByExternalUid(newExternalUid.trim());
                if (existingByUid != null && !existingByUid.getId().equals(id)) {
                    throw new ApiRequestException("Cet external_uid est déjà utilisé par une autre pastille: " + newExternalUid);
                }
            }
        }

        // Mettre à jour les champs
        existingPastille.setCode(refPastilleDTO.getCode().trim());
        existingPastille.setLabel(refPastilleDTO.getLabel().trim());

        // Gérer external_uid
        if (newExternalUid != null && !newExternalUid.trim().isEmpty()) {
            existingPastille.setExternal_uid(newExternalUid.trim());
        } else {
            existingPastille.setExternal_uid(null);
        }

        // Gérer temps théorique
        existingPastille.setTempsTheorique(refPastilleDTO.getTempsTheorique());

        // Gérer audit field
        existingPastille.setAudit_field(refPastilleDTO.getAudit_field());

        // Mettre à jour la date et l'utilisateur de modification
        existingPastille.setUpdated_at(LocalDateTime.now());
        existingPastille.setUpdated_by(userService.getConnectedUserId());

        // Mettre à jour le statut si fourni
        if (refPastilleDTO.getStatus() != null) {
            existingPastille.setStatus(refPastilleDTO.getStatus());
        }

        // MAJ des clés étrangères
        updateForeignKeySite_Secteur(refPastilleDTO, existingPastille);

        return refPastilleRepository.save(existingPastille);
    }

    private void updateForeignKeySite_Secteur(Ref_pastilleDTO refPastilleDTO, Ref_pastille ref_pastille) {
        // Mettre à jour id Site (obligatoire)
        try {
            Ref_site site = refSiteService.findSiteById(refPastilleDTO.getRefSiteId());
            ref_pastille.setRef_site_id(site);
        } catch (Exception e) {
            throw new ApiRequestException("Site non trouvé avec ID: " + refPastilleDTO.getRefSiteId());
        }

        // Mettre à jour id Secteur (optionnel)
        if (refPastilleDTO.getRefSecteurId() != null) {
            try {
                Ref_secteur secteur = refSecteurService.findSecteurById(refPastilleDTO.getRefSecteurId());
                ref_pastille.setRef_secteur_id(secteur);
            } catch (Exception e) {
                throw new ApiRequestException("Secteur non trouvé avec ID: " + refPastilleDTO.getRefSecteurId());
            }
        } else {
            ref_pastille.setRef_secteur_id(null);
        }
    }

    @Override
    @Cacheable(value = "pastilleById", key = "#id")
    public Ref_pastille findPastilleById(Long id) {
        return refPastilleRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Pastille non trouvée avec ID: " + id));
    }

    @Override
    public Ref_pastille findPastilleByExternalUid(String external_uid) {
        if (external_uid == null || external_uid.trim().isEmpty()) {
            throw new ApiRequestException("External UID ne peut pas être vide");
        }

        Ref_pastille ref_pastille = refPastilleRepository.findByExternalUid(external_uid.trim());
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
                .map(String::trim)
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

        Ref_pastille ref_pastille = refPastilleRepository.findByCode(code.trim());
        if (ref_pastille == null) {
            throw new ApiRequestException("Pastille non trouvée avec code: " + code);
        }
        return ref_pastille;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ref_pastille> listRef_pastille() {
        List<Ref_pastille> list = refPastilleRepository.findAll();
        if (list.isEmpty()) {
            throw new ApiRequestException("Pas de pastille enregistrée dans la base de données");
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ref_pastille> findPastilleByIdSite(Long id) {
        // Vérifier que le site existe
        try {
            Ref_site ref_site = refSiteService.findSiteById(id);
            if (ref_site == null) {
                throw new ApiRequestException("Site non trouvé avec ID: " + id);
            }
        } catch (Exception e) {
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
        try {
            Ref_secteur ref_secteur = refSecteurService.findSecteurById(id);
            if (ref_secteur == null) {
                throw new ApiRequestException("Secteur non trouvé avec ID: " + id);
            }
        } catch (Exception e) {
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
        Optional<Ref_pastille> optionalPastille = refPastilleRepository.findById(id);
        if (optionalPastille.isEmpty()) {
            throw new ApiRequestException("Pastille non trouvée avec ID: " + id);
        }

        Ref_pastille pastille = optionalPastille.get();

        // Marquer comme supprimé (soft delete)
        pastille.setIs_deleted(true);
        pastille.setDeleted_at(LocalDateTime.now());
        pastille.setDeleted_by(userService.getConnectedUserId());

        refPastilleRepository.save(pastille);
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