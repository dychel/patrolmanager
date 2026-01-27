package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.RefRondePastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde_pastille;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefRondePastilleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefRondePastilleServiceImpl implements RefRondePastilleService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RefRondePastilleRepository refRondePastilleRepository;

    @Autowired
    private RefRondeService refRondeService;

    @Autowired
    private RefPastilleService refPastilleService;

    @Override
    public Ref_ronde_pastille saveRondePastille(RefRondePastilleDTO refRondePastilleDTO) {
        // Vérifier si la séquence existe déjà pour cette ronde
        Ref_ronde_pastille existing = refRondePastilleRepository.findByRondeIdAndSequence(
                refRondePastilleDTO.getRefRondeId(),
                refRondePastilleDTO.getSeq_no()
        );

        if (existing != null) {
            throw new ApiRequestException("Une pastille avec cette séquence existe déjà pour cette ronde");
        }

        Ref_ronde_pastille ref_ronde_pastille = modelMapper.map(refRondePastilleDTO, Ref_ronde_pastille.class);

        // Mettre à jour les clés étrangères
        updateForeignKeys(refRondePastilleDTO, ref_ronde_pastille);

        // Initialiser l'audit
        ref_ronde_pastille.setCreated_by(userService.getConnectedUserId());
        ref_ronde_pastille.setCreated_at(LocalDateTime.now());

        return refRondePastilleRepository.save(ref_ronde_pastille);
    }

    @Override
    public Ref_ronde_pastille updateRondePastille(Long id, RefRondePastilleDTO refRondePastilleDTO) {
        Ref_ronde_pastille refRondePastilleToUpdate = refRondePastilleRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("RondePastille ID non trouvé"));

        // Vérifier si la nouvelle séquence est unique pour cette ronde (si modifiée)
        if (!refRondePastilleToUpdate.getSeq_no().equals(refRondePastilleDTO.getSeq_no())) {
            Ref_ronde_pastille existing = refRondePastilleRepository.findByRondeIdAndSequence(
                    refRondePastilleDTO.getRefRondeId() != null ?
                            refRondePastilleDTO.getRefRondeId() :
                            refRondePastilleToUpdate.getRef_ronde_id().getId(),
                    refRondePastilleDTO.getSeq_no()
            );

            if (existing != null && !existing.getId().equals(id)) {
                throw new ApiRequestException("Une pastille avec cette séquence existe déjà pour cette ronde");
            }
        }

        // Mapper les modifications
        modelMapper.map(refRondePastilleDTO, refRondePastilleToUpdate);

        // Mettre à jour les clés étrangères
        updateForeignKeys(refRondePastilleDTO, refRondePastilleToUpdate);

        // Mettre à jour l'audit
        refRondePastilleToUpdate.setUpdated_at(LocalDateTime.now());
        refRondePastilleToUpdate.setUpdated_by(userService.getConnectedUserId());

        return refRondePastilleRepository.save(refRondePastilleToUpdate);
    }

    private void updateForeignKeys(RefRondePastilleDTO dto, Ref_ronde_pastille entity) {
        // Mettre à jour la référence ronde
        if (dto.getRefRondeId() != null) {
            Ref_ronde ronde = refRondeService.findRondeById(dto.getRefRondeId());
            entity.setRef_ronde_id(ronde);
        }

        // Mettre à jour la référence pastille
        if (dto.getRefPastilleId() != null) {
            Ref_pastille pastille = refPastilleService.findPastilleById(dto.getRefPastilleId());
            entity.setRef_pastille_id(pastille);
        }
    }

    @Override
    public Ref_ronde_pastille findRondePastilleById(Long id) {
        return refRondePastilleRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("RondePastille non trouvé"));
    }

    @Override
    public List<Ref_ronde_pastille> listRondePastille() {
        List<Ref_ronde_pastille> list = refRondePastilleRepository.findAll();
        if (list.isEmpty()) {
            throw new ApiRequestException("Pas de RondePastille enregistré dans la base de données");
        }
        return list;
    }

    @Override
    public List<Ref_ronde_pastille> findRondePastilleByRondeId(Long rondeId) {
        // Vérifier d'abord si la ronde existe
        refRondeService.findRondeById(rondeId);

        List<Ref_ronde_pastille> list = refRondePastilleRepository.findByRondeId(rondeId);
        if (list.isEmpty()) {
            throw new ApiRequestException("Pas de pastille enregistrée pour cette ronde");
        }
        return list;
    }

    @Override
    public List<Ref_ronde_pastille> findRondePastilleByPastilleId(Long pastilleId) {
        // Vérifier d'abord si la pastille existe
        refPastilleService.findPastilleById(pastilleId);

        List<Ref_ronde_pastille> list = refRondePastilleRepository.findByPastilleId(pastilleId);
        if (list.isEmpty()) {
            throw new ApiRequestException("Cette pastille n'est associée à aucune ronde");
        }
        return list;
    }

    @Override
    public Ref_ronde_pastille findRondePastilleByRondeIdAndSequence(Long rondeId, Integer sequence) {
        // Vérifier d'abord si la ronde existe
        refRondeService.findRondeById(rondeId);

        Ref_ronde_pastille rondePastille = refRondePastilleRepository.findByRondeIdAndSequence(rondeId, sequence);
        if (rondePastille == null) {
            throw new ApiRequestException("Aucune pastille trouvée pour cette ronde avec cette séquence");
        }
        return rondePastille;
    }

    @Override
    public void deleteRondePastilleById(Long id) {
        Ref_ronde_pastille ref_ronde_pastille = refRondePastilleRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("RondePastille non trouvé"));

        refRondePastilleRepository.deleteById(id);
    }

    @Override
    public void deleteRondePastilleByRondeId(Long rondeId) {
        // Vérifier d'abord si la ronde existe
        refRondeService.findRondeById(rondeId);

        List<Ref_ronde_pastille> list = refRondePastilleRepository.findByRondeId(rondeId);
        if (!list.isEmpty()) {
            refRondePastilleRepository.deleteAll(list);
        }
    }

    // Méthode supplémentaire utile
    public List<Ref_ronde_pastille> findRondePastilleByRondeIdOrderBySequence(Long rondeId) {
        // Vérifier d'abord si la ronde existe
        refRondeService.findRondeById(rondeId);

        List<Ref_ronde_pastille> list = refRondePastilleRepository.findByRondeIdOrderBySequence(rondeId);
        if (list.isEmpty()) {
            throw new ApiRequestException("Pas de pastille enregistrée pour cette ronde");
        }
        return list;
    }
}