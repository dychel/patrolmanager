package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.FactPointageDTO;
import com.patrolmanagr.patrolmanagr.entity.*;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.FactPointageRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FactPointageServiceImpl implements FactPointageService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FactPointageRepository factPointageRepository;

    @Autowired
    private RefSiteService refSiteService;

    @Autowired
    private RefPastilleService refPastilleService;

    @Autowired
    private RefRondeService refRondeService;

    @Autowired
    private RefTerminalService refTerminalService;

//    public Fact_pointage savePointage(FactPointageDTO factPointageDTO) {
//        Fact_pointage factPointage = modelMapper.map(factPointageDTO, Fact_pointage.class);
//
//        // Validation et enrichissement
//        validateAndEnrichPointage(factPointage);
//
//        // Vérifier les doublons
//        if (factPointageRepository.existsByUniqueKey(
//                factPointage.getEventTime(),
//                factPointage.getPastilleCodeRaw(),
//                factPointage.getTerminalCodeRaw())) {
//            throw new ApiRequestException("Un pointage similaire existe déjà");
//        }
//
//        factPointage.setCreatedAt(LocalDateTime.now());
//
//        return factPointageRepository.save(factPointage);
//    }

    @Override
    @Transactional
    public Fact_pointage savePointage(FactPointageDTO factPointageDTO) {
        // Créer manuellement au lieu d'utiliser ModelMapper
        Fact_pointage factPointage = new Fact_pointage();

        // Mapper manuellement les champs
        factPointage.setEventTime(factPointageDTO.getEventTime());
        factPointage.setRondeName(factPointageDTO.getRondeName());
        factPointage.setRondeId(factPointageDTO.getRondeId());
        factPointage.setEventDate(factPointageDTO.getEventTime() != null ?
                factPointageDTO.getEventTime().toLocalDate() : null);
        factPointage.setSiteId(factPointageDTO.getSiteId());
        factPointage.setPastilleId(factPointageDTO.getPastilleId());
        factPointage.setPastilleCodeRaw(factPointageDTO.getPastilleCodeRaw());
        factPointage.setTerminalId(factPointageDTO.getTerminalId());
        factPointage.setTerminalCodeRaw(factPointageDTO.getTerminalCodeRaw());
        factPointage.setAgentUserId(factPointageDTO.getAgentUserId());
        factPointage.setAgentCodeRaw(factPointageDTO.getAgentCodeRaw());
        factPointage.setSiteName(factPointageDTO.getSiteName());
        factPointage.setZoneId(factPointageDTO.getZoneId());
        factPointage.setZoneName(factPointageDTO.getZoneName());
        factPointage.setSecteurId(factPointageDTO.getSecteurId());
        factPointage.setSecteurName(factPointageDTO.getSecteurName());
        factPointage.setPastilleLabel(factPointageDTO.getPastilleLabel());
        factPointage.setTerminalType(factPointageDTO.getTerminalType());
        factPointage.setVendorId(factPointageDTO.getVendorId());
        factPointage.setSourceType(factPointageDTO.getSourceType());
        factPointage.setSourceBatchId(factPointageDTO.getSourceBatchId());

        // Validation et enrichissement
        validateAndEnrichPointage(factPointage);

        // Vérifier les doublons
        if (factPointageRepository.existsByUniqueKey(
                factPointage.getEventTime(),
                factPointage.getPastilleCodeRaw(),
                factPointage.getTerminalCodeRaw())) {
            throw new ApiRequestException("Un pointage similaire existe déjà");
        }

        factPointage.setCreatedAt(LocalDateTime.now());

        return factPointageRepository.save(factPointage);
    }

    @Override
    @Transactional
    public List<Fact_pointage> savePointageBatch(List<FactPointageDTO> pointagesDTO) {
        if (pointagesDTO == null || pointagesDTO.isEmpty()) {
            return List.of();
        }

        // 1. Extraire tous les external_uids pour recherche batch
        List<String> externalUids = pointagesDTO.stream()
                .map(FactPointageDTO::getPastilleCodeRaw)
                .filter(uid -> uid != null && !uid.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 2. Récupérer toutes les pastilles en une seule requête
        Map<String, Ref_pastille> pastilleMap = refPastilleService.getPastilleMapByExternalUids(externalUids);

        // 3. Traiter chaque pointage
        List<Fact_pointage> savedPointages = new ArrayList<>();
        List<Fact_pointage> pointagesToSave = new ArrayList<>();

        for (FactPointageDTO dto : pointagesDTO) {
            try {
                Fact_pointage pointage = modelMapper.map(dto, Fact_pointage.class);

                // Enrichissement batch optimisé
                enrichPointageFromMap(pointage, pastilleMap);

                // Validation supplémentaire
                validatePointage(pointage);

                pointage.setCreatedAt(LocalDateTime.now());
                pointagesToSave.add(pointage);

            } catch (Exception e) {
                // Log l'erreur mais continue avec les autres
                System.err.println("Erreur traitement pointage: " + e.getMessage());
            }
        }

        // 4. Sauvegarde batch
        if (!pointagesToSave.isEmpty()) {
            savedPointages = factPointageRepository.saveAll(pointagesToSave);
        }

        return savedPointages;
    }
    private void enrichPointageFromMap(Fact_pointage pointage, Map<String, Ref_pastille> pastilleMap) {
        // 1. Gestion de la pastille via external_uid
        if (pointage.getPastilleCodeRaw() != null) {
            Ref_pastille pastille = pastilleMap.get(pointage.getPastilleCodeRaw());

            if (pastille != null) {
                // Pastille trouvée → enrichir
                pointage.setPastilleId(pastille.getId());
                pointage.setPastilleLabel(pastille.getLabel());

                //trouver le site, terminal et ronde
                Ref_pastille ref_pastille = refPastilleService.findPastilleById(pastille.getId());
                // Secteur
                if (pastille.getRef_secteur_id() != null) {
                    pointage.setSecteurId(pastille.getRef_secteur_id().getId());
                    pointage.setSecteurName(pastille.getRef_secteur_id().getName());
                }

                // Site (priorité: site de la pastille > site fourni)
                if (pastille.getRef_site_id() != null) {
                    pointage.setSiteId(pastille.getRef_site_id().getId());
                    pointage.setSiteName(pastille.getRef_site_id().getName());

                    // Zone
                    if (pastille.getRef_site_id().getRef_zone() != null) {
                        pointage.setZoneId(pastille.getRef_site_id().getRef_zone().getId());
                        pointage.setZoneName(pastille.getRef_site_id().getRef_zone().getName());
                    }

                    // NOUVEAU : Récupérer la ronde active du site
                    if (pointage.getRondeId() == null) {
                        Long rondeId = findActiveRondeForSite(pastille.getRef_site_id().getId());
                        if (rondeId != null) {
                            Ref_ronde ref_ronde = refRondeService.findRondeById(rondeId);
                            pointage.setRondeId(rondeId);
                            pointage.setRondeName(ref_ronde.getCode());
                        }
                    }
                }

                pointage.setProcessedStatus("PROCESSED");

            } else {
                // Pastille inconnue → marquer pour rejet
                pointage.setProcessedStatus("REJECTED");
                pointage.setRejectionReason("Pastille inconnue: " + pointage.getPastilleCodeRaw());
            }
        }

        // 2. Si pas de site défini via la pastille, utiliser celui fourni
        if (pointage.getSiteId() != null && pointage.getSiteName() == null) {
            try {
                var site = refSiteService.findSiteById(pointage.getSiteId());
                pointage.setSiteName(site.getName());
                //Added some data
                Ref_ronde ref_ronde = refRondeService.findActiveRondeBySiteId(pointage.getSiteId());
                pointage.setRondeName(ref_ronde.getCode());
//                Ref_terminal ref_terminal = refTerminalService.findTerminalByIdSite(pointage.getSiteId());
//                pointage.setTerminalId(ref_terminal.getId());
//                pointage.setTerminalCodeRaw(ref_terminal.getCode());
//                pointage.setTerminalType(ref_terminal.getTerminalType());
                //

                if (site.getRef_zone() != null) {
                    pointage.setZoneId(site.getRef_zone().getId());
                    pointage.setZoneName(site.getRef_zone().getName());
                }

                // NOUVEAU : Récupérer la ronde active du site
                if (pointage.getRondeId() == null) {
                    Long rondeId = findActiveRondeForSite(pointage.getSiteId());
                    if (rondeId != null) {
                        pointage.setRondeId(rondeId);
                    }
                }

            } catch (Exception e) {
                pointage.setProcessedStatus("REJECTED");
                pointage.setRejectionReason("Site invalide: " + pointage.getSiteId());
            }
        }

        // 3. Validation de la ronde (si maintenant fournie)
        if (pointage.getRondeId() != null) {
            try {
                refRondeService.findRondeById(pointage.getRondeId());
            } catch (Exception e) {
                pointage.setProcessedStatus("REJECTED");
                pointage.setRejectionReason("Ronde invalide: " + pointage.getRondeId());
            }
        }

        // 4. Si pas de statut défini, mettre PENDING
        if (pointage.getProcessedStatus() == null) {
            pointage.setProcessedStatus("PENDING");
        }
    }

    // NOUVELLE MÉTHODE : Trouver la ronde active pour un site
    private Long findActiveRondeForSite(Long siteId) {
        try {
            // Récupérer les rondes actives du site
            List<Ref_ronde> rondes = refRondeService.findRondeByIdSite(siteId);

            // Filtrer les rondes actives (status = ACTIVE)
            List<Ref_ronde> rondesActives = rondes.stream()
                    .filter(r -> r.getStatus() == Status.ACTIVE)
                    .toList();

            // Prendre la première ronde active
            if (!rondesActives.isEmpty()) {
                return rondesActives.get(0).getId();
            }

            // Sinon, prendre la première ronde (même inactive)
            if (!rondes.isEmpty()) {
                return rondes.get(0).getId();
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
    private void validatePointage(Fact_pointage pointage) {
        // Validation de base
        if (pointage.getEventTime() == null) {
            pointage.setProcessedStatus("REJECTED");
            pointage.setRejectionReason("eventTime est obligatoire");
        }

        if (pointage.getSiteId() == null) {
            pointage.setProcessedStatus("REJECTED");
            pointage.setRejectionReason("siteId est obligatoire");
        }

        if (pointage.getPastilleCodeRaw() == null) {
            pointage.setProcessedStatus("REJECTED");
            pointage.setRejectionReason("pastilleCodeRaw est obligatoire");
        }
    }

    // Méthodes restantes inchangées...
    @Override
    public Fact_pointage updatePointage(Long id, FactPointageDTO factPointageDTO) {
        Fact_pointage existingPointage = findPointageById(id);

        Fact_pointage updatedPointage = modelMapper.map(factPointageDTO, Fact_pointage.class);
        updatedPointage.setId(id);
        updatedPointage.setCreatedAt(existingPointage.getCreatedAt());

        // Re-validation si nécessaire
        if (!"PROCESSED".equals(existingPointage.getProcessedStatus())) {
            validateAndEnrichPointage(updatedPointage);
        }

        return factPointageRepository.save(updatedPointage);
    }

    @Override
    public Fact_pointage findPointageById(Long id) {
        return factPointageRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Pointage non trouvé avec ID: " + id));
    }

    @Override
    public List<Fact_pointage> findAllPointages() {
        List<Fact_pointage> pointages = factPointageRepository.findAll();
        if (pointages.isEmpty()) {
            throw new ApiRequestException("Aucun pointage enregistré");
        }
        return pointages;
    }

    @Override
    public void deletePointageById(Long id) {
        if (!factPointageRepository.existsById(id)) {
            throw new ApiRequestException("Pointage non trouvé avec ID: " + id);
        }
        factPointageRepository.deleteById(id);
    }

    @Override
    public List<Fact_pointage> findBySiteAndPeriod(Long siteId, LocalDate startDate, LocalDate endDate) {
        return factPointageRepository.findBySiteIdAndEventDateBetween(siteId, startDate, endDate);
    }

    @Override
    public List<Fact_pointage> findByRondeAndPeriod(Long rondeId, LocalDate startDate, LocalDate endDate) {
        return factPointageRepository.findByRondeIdAndEventDateBetween(rondeId, startDate, endDate);
    }

    @Override
    public List<Fact_pointage> findByAgent(Long agentUserId) {
        return factPointageRepository.findByAgentUserId(agentUserId);
    }

    @Override
    public List<Fact_pointage> findRejectedPointages() {
        return factPointageRepository.findByProcessedStatus("REJECTED");
    }

    @Override
    public List<Fact_pointage> findPendingPointages() {
        return factPointageRepository.findByProcessedStatus("PENDING");
    }

    @Override
    public Fact_pointage validatePointage(Long id, String validationNotes) {
        Fact_pointage pointage = findPointageById(id);
        pointage.setProcessedStatus("PROCESSED");
        pointage.setValidationNotes(validationNotes);
        return factPointageRepository.save(pointage);
    }

    @Override
    public Fact_pointage rejectPointage(Long id, String rejectionReason) {
        Fact_pointage pointage = findPointageById(id);
        pointage.setProcessedStatus("REJECTED");
        pointage.setRejectionReason(rejectionReason);
        return factPointageRepository.save(pointage);
    }

    @Override
    public List<Fact_pointage> findByExternalUid(String externalUid) {
        return factPointageRepository.findByPastilleCodeRaw(externalUid);
    }

    private void validateAndEnrichPointage(Fact_pointage factPointage) {
        // Version individuelle (moins optimisée)
        try {
            // Chercher la pastille par external_uid
            if (factPointage.getPastilleCodeRaw() != null && factPointage.getPastilleId() == null) {
                Ref_pastille pastille = refPastilleService.findPastilleByExternalUid(factPointage.getPastilleCodeRaw());
                factPointage.setPastilleId(pastille.getId());
                factPointage.setPastilleLabel(pastille.getLabel());

                // Secteur
                if (pastille.getRef_secteur_id() != null) {
                    factPointage.setSecteurId(pastille.getRef_secteur_id().getId());
                    factPointage.setSecteurName(pastille.getRef_secteur_id().getName());
                }

                // Site
                if (pastille.getRef_site_id() != null) {
                    factPointage.setSiteId(pastille.getRef_site_id().getId());
                    factPointage.setSiteName(pastille.getRef_site_id().getName());

                    // Zone
                    if (pastille.getRef_site_id().getRef_zone() != null) {
                        factPointage.setZoneId(pastille.getRef_site_id().getRef_zone().getId());
                        factPointage.setZoneName(pastille.getRef_site_id().getRef_zone().getName());
                    }
                }

                // NOUVEAU : Récupérer la ronde active du site
                if (factPointage.getRondeId() == null) {
                    Long rondeId = findActiveRondeForSite(factPointage.getSiteId());
                    if (rondeId != null) {
                        Ref_ronde ref_ronde = refRondeService.findRondeById(rondeId);
                        factPointage.setRondeName(ref_ronde.getCode());
                        factPointage.setRondeName(ref_ronde.getCode());
                    }
                }

                factPointage.setProcessedStatus("PROCESSED");
            }
        } catch (ApiRequestException e) {
            factPointage.setProcessedStatus("REJECTED");
            factPointage.setRejectionReason("Pastille inconnue: " + factPointage.getPastilleCodeRaw());
        }
    }
}