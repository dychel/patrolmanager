package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.ExecRondePastilleDTO;
import com.patrolmanagr.patrolmanagr.entity.exec_ronde_pastille;
import com.patrolmanagr.patrolmanagr.entity.exec_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_pastille;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde_pastille;
import com.patrolmanagr.patrolmanagr.config.Status_ronde_pastille;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.ExecRondePastilleRepository;
import com.patrolmanagr.patrolmanagr.repository.RefRondePastilleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExecRondePastilleServiceImpl implements ExecRondePastilleService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserService userService;

    @Autowired
    ExecRondePastilleRepository execRondePastilleRepository;

    @Autowired
    ExecRondeService execRondeService;

    @Autowired
    RefPastilleService refPastilleService;

    @Autowired
    RefRondePastilleRepository refRondePastilleRepository;

    @Override
    public exec_ronde_pastille saveExecRondePastille(ExecRondePastilleDTO execRondePastilleDTO) {
        // Vérifier l'unicité de la séquence pour cette exécution
        boolean exists = execRondePastilleRepository.existsByExecRondeIdAndSeqNo(
                execRondePastilleDTO.getExecRondeId(),
                execRondePastilleDTO.getSeqNo()
        );

        if (exists) {
            throw new ApiRequestException("Une pastille avec cette séquence existe déjà pour cette exécution de ronde");
        }

        exec_ronde_pastille exec_ronde_pastille = modelMapper.map(execRondePastilleDTO, exec_ronde_pastille.class);
        exec_ronde_pastille.setCreated_by(userService.getConnectedUserId());
        exec_ronde_pastille.setCreated_at(LocalDateTime.now());

        // Mettre à jour les clés étrangères
        updateForeignKeys(execRondePastilleDTO, exec_ronde_pastille);

        // Initialiser le statut si non fourni
        if (exec_ronde_pastille.getStatus() == null) {
            exec_ronde_pastille.setStatus(Status_ronde_pastille.EXPECTED);
        }

        return execRondePastilleRepository.save(exec_ronde_pastille);
    }

    @Override
    public exec_ronde_pastille updateExecRondePastille(Long id, ExecRondePastilleDTO execRondePastilleDTO) {
        exec_ronde_pastille execRondePastilleToUpdate = execRondePastilleRepository.findByIdExecRondePastille(id);
        if (execRondePastilleToUpdate == null)
            throw new ApiRequestException("ExecRondePastille ID non trouvé");

        // Vérifier l'unicité de la séquence si modifiée
        if (!execRondePastilleToUpdate.getSeqNo().equals(execRondePastilleDTO.getSeqNo()) ||
                !execRondePastilleToUpdate.getExecRonde().getId().equals(execRondePastilleDTO.getExecRondeId())) {

            boolean exists = execRondePastilleRepository.existsByExecRondeIdAndSeqNo(
                    execRondePastilleDTO.getExecRondeId(),
                    execRondePastilleDTO.getSeqNo()
            );

            if (exists) {
                throw new ApiRequestException("Une pastille avec cette séquence existe déjà pour cette exécution de ronde");
            }
        }

        exec_ronde_pastille exec_ronde_pastille = modelMapper.map(execRondePastilleDTO, exec_ronde_pastille.class);
        exec_ronde_pastille.setId(id);
        exec_ronde_pastille.setUpdated_at(LocalDateTime.now());
        exec_ronde_pastille.setUpdated_by(userService.getConnectedUserId());
        exec_ronde_pastille.setCreated_at(execRondePastilleToUpdate.getCreated_at());
        exec_ronde_pastille.setCreated_by(execRondePastilleToUpdate.getCreated_by());

        // Mettre à jour les clés étrangères
        updateForeignKeys(execRondePastilleDTO, exec_ronde_pastille);

        return execRondePastilleRepository.save(exec_ronde_pastille);
    }

    private void updateForeignKeys(ExecRondePastilleDTO execRondePastilleDTO, exec_ronde_pastille exec_ronde_pastille) {
        // Mettre à jour id ExecRonde
        if (execRondePastilleDTO.getExecRondeId() != null) {
            exec_ronde execRonde = execRondeService.findExecRondeById(execRondePastilleDTO.getExecRondeId());
            exec_ronde_pastille.setExecRonde(execRonde);
        }

        // Mettre à jour id Pastille
        if (execRondePastilleDTO.getPastilleId() != null) {
            Ref_pastille pastille = refPastilleService.findPastilleById(execRondePastilleDTO.getPastilleId());
            exec_ronde_pastille.setPastille(pastille);
        }
    }

    @Override
    public exec_ronde_pastille findExecRondePastilleById(Long id) {
        exec_ronde_pastille execRondePastilleToUpdate = execRondePastilleRepository.findByIdExecRondePastille(id);
        if (execRondePastilleToUpdate == null)
            throw new ApiRequestException("ExecRondePastille non trouvé");
        return execRondePastilleToUpdate;
    }

    @Override
    public List<exec_ronde_pastille> listExecRondePastille() {
        List<exec_ronde_pastille> exec_ronde_pastilles = execRondePastilleRepository.findAll();
        if (exec_ronde_pastilles.isEmpty())
            throw new ApiRequestException("Pas d'exécutions de pastilles enregistrées dans la base de données");
        return exec_ronde_pastilles;
    }

    @Override
    public List<exec_ronde_pastille> findExecRondePastilleByExecRondeId(Long execRondeId) {
        execRondeService.findExecRondeById(execRondeId); // Vérifie si l'exécution existe
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleRepository.findByExecRondeId(execRondeId);
        if (execRondePastilles.isEmpty())
            throw new ApiRequestException("Pas de pastille trouvée pour cette exécution de ronde");
        return execRondePastilles;
    }

    @Override
    public List<exec_ronde_pastille> findExecRondePastilleByPastilleId(Long pastilleId) {
        refPastilleService.findPastilleById(pastilleId); // Vérifie si la pastille existe
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleRepository.findByPastilleId(pastilleId);
        if (execRondePastilles.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour cette pastille");
        return execRondePastilles;
    }

    @Override
    public exec_ronde_pastille findExecRondePastilleByExecRondeIdAndSeqNo(Long execRondeId, Integer seqNo) {
        execRondeService.findExecRondeById(execRondeId); // Vérifie si l'exécution existe
        exec_ronde_pastille execRondePastille = execRondePastilleRepository.findByExecRondeIdAndSeqNo(execRondeId, seqNo);
        if (execRondePastille == null)
            throw new ApiRequestException("Pas de pastille trouvée pour cette exécution et cette séquence");
        return execRondePastille;
    }

    @Override
    public List<exec_ronde_pastille> findExecRondePastilleByExecRondeIdOrderBySeqNo(Long execRondeId) {
        execRondeService.findExecRondeById(execRondeId); // Vérifie si l'exécution existe
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleRepository.findByExecRondeIdOrderBySeqNo(execRondeId);
        if (execRondePastilles.isEmpty())
            throw new ApiRequestException("Pas de pastille trouvée pour cette exécution de ronde");
        return execRondePastilles;
    }

    @Override
    public List<exec_ronde_pastille> findExecRondePastilleByStatus(Status_ronde_pastille status) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleRepository.findByStatus(status);
        if (execRondePastilles.isEmpty())
            throw new ApiRequestException("Pas d'exécution de pastille trouvée avec ce statut");
        return execRondePastilles;
    }

    @Override
    public List<exec_ronde_pastille> findExecRondePastilleByExecRondeIdAndStatus(Long execRondeId, Status_ronde_pastille status) {
        execRondeService.findExecRondeById(execRondeId); // Vérifie si l'exécution existe
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleRepository.findByExecRondeIdAndStatus(execRondeId, status);
        if (execRondePastilles.isEmpty())
            throw new ApiRequestException("Pas de pastille trouvée pour cette exécution avec ce statut");
        return execRondePastilles;
    }

    @Override
    public List<exec_ronde_pastille> findExecRondePastilleByScannedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ApiRequestException("La date de début doit être avant la date de fin");
        }

        List<exec_ronde_pastille> execRondePastilles = execRondePastilleRepository.findByScannedAtBetween(startDate, endDate);
        if (execRondePastilles.isEmpty())
            throw new ApiRequestException("Pas d'exécution de pastille trouvée dans cette période");
        return execRondePastilles;
    }

    @Override
    public List<exec_ronde_pastille> findExecRondePastilleByPointageId(Long pointageId) {
        List<exec_ronde_pastille> execRondePastilles = execRondePastilleRepository.findByPointageId(pointageId);
        if (execRondePastilles.isEmpty())
            throw new ApiRequestException("Pas d'exécution de pastille trouvée pour ce pointage");
        return execRondePastilles;
    }

    @Override
    public void deleteExecRondePastilleById(Long id) {
        exec_ronde_pastille exec_ronde_pastille = execRondePastilleRepository.findByIdExecRondePastille(id);
        if (exec_ronde_pastille == null)
            throw new ApiRequestException("ExecRondePastille non trouvé");
        execRondePastilleRepository.deleteById(id);
    }

    @Override
    public exec_ronde_pastille markAsDone(Long id, LocalDateTime scannedAt, Integer actualTravelSec, String notes) {
        exec_ronde_pastille execRondePastille = findExecRondePastilleById(id);

        if (execRondePastille.getStatus() == Status_ronde_pastille.DONE) {
            throw new ApiRequestException("Cette pastille est déjà marquée comme DONE");
        }

        execRondePastille.setStatus(Status_ronde_pastille.DONE);
        execRondePastille.setScannedAt(scannedAt != null ? scannedAt : LocalDateTime.now());
        execRondePastille.setActualTime(LocalDateTime.now());

        if (actualTravelSec != null) {
            execRondePastille.setActualTravelSec(actualTravelSec);

            // Calculer la déviation si expectedTravelSec est défini
            if (execRondePastille.getExpectedTravelSec() != null) {
                execRondePastille.setDeviationSec(actualTravelSec - execRondePastille.getExpectedTravelSec());
            }
        }

        if (notes != null) {
            execRondePastille.setNotes(notes);
        }

        execRondePastille.setUpdated_at(LocalDateTime.now());
        execRondePastille.setUpdated_by(userService.getConnectedUserId());

        return execRondePastilleRepository.save(execRondePastille);
    }

    @Override
    public exec_ronde_pastille markAsMissed(Long id, String notes) {
        exec_ronde_pastille execRondePastille = findExecRondePastilleById(id);

        if (execRondePastille.getStatus() == Status_ronde_pastille.DONE) {
            throw new ApiRequestException("Impossible de marquer comme MISSED une pastille déjà DONE");
        }

        execRondePastille.setStatus(Status_ronde_pastille.MISSED);

        if (notes != null) {
            execRondePastille.setNotes(notes);
        }

        execRondePastille.setUpdated_at(LocalDateTime.now());
        execRondePastille.setUpdated_by(userService.getConnectedUserId());

        return execRondePastilleRepository.save(execRondePastille);
    }

    @Override
    public exec_ronde_pastille updatePointage(Long id, Long pointageId) {
        exec_ronde_pastille execRondePastille = findExecRondePastilleById(id);
        execRondePastille.setPointageId(pointageId);
        execRondePastille.setUpdated_at(LocalDateTime.now());
        execRondePastille.setUpdated_by(userService.getConnectedUserId());

        return execRondePastilleRepository.save(execRondePastille);
    }

    @Override
    public List<exec_ronde_pastille> initializeFromRonde(Long execRondeId) {
        exec_ronde execRonde = execRondeService.findExecRondeById(execRondeId);

        // Récupérer les pastilles de la ronde de référence
        List<Ref_ronde_pastille> refPastilles = refRondePastilleRepository.findByRondeIdOrderBySequence(
                execRonde.getRefRonde().getId()
        );

        if (refPastilles.isEmpty()) {
            throw new ApiRequestException("Aucune pastille définie pour cette ronde");
        }

        // Convertir les pastilles de référence en exécutions de pastilles
        return refPastilles.stream().map(refPastille -> {
            ExecRondePastilleDTO dto = new ExecRondePastilleDTO();
            dto.setExecRondeId(execRondeId);
            dto.setPastilleId(refPastille.getRef_pastille_id().getId());
            dto.setSeqNo(refPastille.getSeq_no());
            dto.setExpectedTravelSec(refPastille.getExpected_travel_sec());
            dto.setStatus(Status_ronde_pastille.EXPECTED);
            dto.setExpectedTime(execRonde.getPlannedStartAt());

            return saveExecRondePastille(dto);
        }).collect(Collectors.toList());
    }

    @Override
    public Long countByExecRondeIdAndStatus(Long execRondeId, Status_ronde_pastille status) {
        execRondeService.findExecRondeById(execRondeId); // Vérifie si l'exécution existe
        return execRondePastilleRepository.countByExecRondeIdAndStatus(execRondeId, status);
    }
}