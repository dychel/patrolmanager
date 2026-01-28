package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.config.Status_exec_Ronde;
import com.patrolmanagr.patrolmanagr.dto.ExecRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.Exec_ronde;
import com.patrolmanagr.patrolmanagr.entity.Prog_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.ExecRondeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ExecRondeServiceImpl implements ExecRondeService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserService userService;

    @Autowired
    ExecRondeRepository execRondeRepository;

    @Autowired
    ProgRondeService progRondeService;

    @Autowired
    RefRondeService refRondeService;

    @Autowired
    RefSiteService refSiteService;

    @Override
    public Exec_ronde saveExecRonde(ExecRondeDTO execRondeDTO) {
        // Vérifier la contrainte d'unicité
        boolean exists = execRondeRepository.existsByProgRondeAndPlannedStartAt(
                execRondeDTO.getProgRondeId(),
                execRondeDTO.getPlannedStartAt()
        );

        if (exists) {
            throw new ApiRequestException("Une exécution existe déjà pour cette programmation et cette date de début");
        }

        // Vérifier que plannedEndAt est après plannedStartAt
        if (execRondeDTO.getPlannedEndAt().isBefore(execRondeDTO.getPlannedStartAt())) {
            throw new ApiRequestException("La date de fin prévue doit être après la date de début prévue");
        }

        Exec_ronde exec_ronde = modelMapper.map(execRondeDTO, Exec_ronde.class);
        exec_ronde.setCreated_by(userService.getConnectedUserId());
        exec_ronde.setCreated_at(LocalDateTime.now());

        // Mettre à jour les clés étrangères
        updateForeignKeys(execRondeDTO, exec_ronde);

        // Initialiser le statut si non fourni
        if (exec_ronde.getStatus() == null) {
            exec_ronde.setStatus(Status_exec_Ronde.PLANNED);
        }

        return execRondeRepository.save(exec_ronde);
    }

    @Override
    public Exec_ronde updateExecRonde(Long id, ExecRondeDTO execRondeDTO) {
        Exec_ronde execRondeToUpdate = execRondeRepository.findByIdExecRonde(id);
        if (execRondeToUpdate == null)
            throw new ApiRequestException("ExecRonde ID non trouvé");

        // Vérifier la contrainte d'unicité si plannedStartAt est modifié
        if (!execRondeToUpdate.getPlannedStartAt().equals(execRondeDTO.getPlannedStartAt()) ||
                !execRondeToUpdate.getProgRonde().getId().equals(execRondeDTO.getProgRondeId())) {

            boolean exists = execRondeRepository.existsByProgRondeAndPlannedStartAt(
                    execRondeDTO.getProgRondeId(),
                    execRondeDTO.getPlannedStartAt()
            );

            if (exists) {
                throw new ApiRequestException("Une exécution existe déjà pour cette programmation et cette date de début");
            }
        }

        // Vérifier que plannedEndAt est après plannedStartAt
        if (execRondeDTO.getPlannedEndAt().isBefore(execRondeDTO.getPlannedStartAt())) {
            throw new ApiRequestException("La date de fin prévue doit être après la date de début prévue");
        }

        Exec_ronde exec_ronde = modelMapper.map(execRondeDTO, Exec_ronde.class);
        exec_ronde.setId(id);
        exec_ronde.setUpdated_at(LocalDateTime.now());
        exec_ronde.setUpdated_by(userService.getConnectedUserId());
        exec_ronde.setCreated_at(execRondeToUpdate.getCreated_at());
        exec_ronde.setCreated_by(execRondeToUpdate.getCreated_by());

        // Mettre à jour les clés étrangères
        updateForeignKeys(execRondeDTO, exec_ronde);

        return execRondeRepository.save(exec_ronde);
    }

    private void updateForeignKeys(ExecRondeDTO execRondeDTO, Exec_ronde exec_ronde) {
        // Mettre à jour id ProgRonde
        if (execRondeDTO.getProgRondeId() != null) {
            Prog_ronde progRonde = progRondeService.findProgRondeById(execRondeDTO.getProgRondeId());
            exec_ronde.setProgRonde(progRonde);
        }

        // Mettre à jour id RefRonde
        if (execRondeDTO.getRefRondeId() != null) {
            Ref_ronde refRonde = refRondeService.findRondeById(execRondeDTO.getRefRondeId());
            exec_ronde.setRefRonde(refRonde);
        }

        // Mettre à jour id Site
        if (execRondeDTO.getSiteId() != null) {
            Ref_site site = refSiteService.findSiteById(execRondeDTO.getSiteId());
            exec_ronde.setSite(site);
        }
    }

    @Override
    public Exec_ronde findExecRondeById(Long id) {
        Exec_ronde execRondeToUpdate = execRondeRepository.findByIdExecRonde(id);
        if (execRondeToUpdate == null)
            throw new ApiRequestException("ExecRonde non trouvé");
        return execRondeToUpdate;
    }

    @Override
    public List<Exec_ronde> listExecRonde() {
        List<Exec_ronde> Exec_rondes = execRondeRepository.findAll();
        if (Exec_rondes.isEmpty())
            throw new ApiRequestException("Pas d'exécutions de ronde enregistrées dans la base de données");
        return Exec_rondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeByProgRondeId(Long progRondeId) {
        progRondeService.findProgRondeById(progRondeId); // Vérifie si la programmation existe
        List<Exec_ronde> execRondes = execRondeRepository.findByProgRondeId(progRondeId);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour cette programmation");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeByRefRondeId(Long refRondeId) {
        refRondeService.findRondeById(refRondeId); // Vérifie si la ronde existe
        List<Exec_ronde> execRondes = execRondeRepository.findByRefRondeId(refRondeId);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour cette ronde");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeBySiteId(Long siteId) {
        refSiteService.findSiteById(siteId); // Vérifie si le site existe
        List<Exec_ronde> execRondes = execRondeRepository.findBySiteId(siteId);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour ce site");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeByExecDate(LocalDate execDate) {
        List<Exec_ronde> execRondes = execRondeRepository.findByExecDate(execDate);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour cette date");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeByStatus(Status_exec_Ronde status) {
        List<Exec_ronde> execRondes = execRondeRepository.findByStatus(status);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée avec ce statut");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeBySiteIdAndExecDate(Long siteId, LocalDate execDate) {
        refSiteService.findSiteById(siteId); // Vérifie si le site existe
        List<Exec_ronde> execRondes = execRondeRepository.findBySiteIdAndExecDate(siteId, execDate);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour ce site et cette date");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeByExecDateAndStatus(LocalDate execDate, Status_exec_Ronde status) {
        List<Exec_ronde> execRondes = execRondeRepository.findByExecDateAndStatus(execDate, status);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour cette date et ce statut");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeBySiteIdAndExecDateAndStatus(Long siteId, LocalDate execDate, Status_exec_Ronde status) {
        refSiteService.findSiteById(siteId); // Vérifie si le site existe
        List<Exec_ronde> execRondes = execRondeRepository.findBySiteIdAndExecDateAndStatus(siteId, execDate, status);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée pour ce site, cette date et ce statut");
        return execRondes;
    }

    @Override
    public List<Exec_ronde> findExecRondeByPlannedStartAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ApiRequestException("La date de début doit être avant la date de fin");
        }

        List<Exec_ronde> execRondes = execRondeRepository.findByPlannedStartAtBetween(startDate, endDate);
        if (execRondes.isEmpty())
            throw new ApiRequestException("Pas d'exécution trouvée dans cette période");
        return execRondes;
    }

    @Override
    public void deleteExecRondeById(Long id) {
        Exec_ronde exec_ronde = execRondeRepository.findByIdExecRonde(id);
        if (exec_ronde == null)
            throw new ApiRequestException("ExecRonde non trouvé");
        execRondeRepository.deleteById(id);
    }

    @Override
    public Exec_ronde updateExecRondeStatus(Long id, Status_exec_Ronde status, BigDecimal completionRate) {
        Exec_ronde execRonde = findExecRondeById(id);

        // Validation du taux de complétion
        if (completionRate != null &&
                (completionRate.compareTo(BigDecimal.ZERO) < 0 || completionRate.compareTo(new BigDecimal("100")) > 0)) {
            throw new ApiRequestException("Le taux de complétion doit être entre 0 et 100");
        }

        execRonde.setStatus(status);
        execRonde.setCompletionRate(completionRate);
        execRonde.setUpdated_at(LocalDateTime.now());
        execRonde.setUpdated_by(userService.getConnectedUserId());

        return execRondeRepository.save(execRonde);
    }

    @Override
    public Exec_ronde startExecRonde(Long id) {
        Exec_ronde execRonde = findExecRondeById(id);

        if (execRonde.getStatus() == Status_exec_Ronde.IN_PROGRESS) {
            throw new ApiRequestException("Cette exécution est déjà en cours");
        }

        if (execRonde.getStatus() == Status_exec_Ronde.DONE || execRonde.getStatus() == Status_exec_Ronde.CANCELLED) {
            throw new ApiRequestException("Impossible de démarrer une exécution terminée ou annulée");
        }

        execRonde.setStatus(Status_exec_Ronde.IN_PROGRESS);
        execRonde.setStartedAt(LocalDateTime.now());
        execRonde.setLastEventAt(LocalDateTime.now());
        execRonde.setUpdated_at(LocalDateTime.now());
        execRonde.setUpdated_by(userService.getConnectedUserId());

        return execRondeRepository.save(execRonde);
    }

    @Override
    public Exec_ronde endExecRonde(Long id, BigDecimal completionRate) {
        Exec_ronde execRonde = findExecRondeById(id);

        if (execRonde.getStatus() != Status_exec_Ronde.IN_PROGRESS) {
            throw new ApiRequestException("Seule une exécution en cours peut être terminée");
        }

        // Validation du taux de complétion
        if (completionRate == null ||
                completionRate.compareTo(BigDecimal.ZERO) < 0 ||
                completionRate.compareTo(new BigDecimal("100")) > 0) {
            throw new ApiRequestException("Le taux de complétion doit être fourni et être entre 0 et 100");
        }

        execRonde.setStatus(Status_exec_Ronde.DONE);
        execRonde.setCompletionRate(completionRate);
        execRonde.setEndedAt(LocalDateTime.now());
        execRonde.setLastEventAt(LocalDateTime.now());
        execRonde.setUpdated_at(LocalDateTime.now());
        execRonde.setUpdated_by(userService.getConnectedUserId());

        return execRondeRepository.save(execRonde);
    }

    @Override
    public Exec_ronde updateLastEvent(Long id) {
        Exec_ronde execRonde = findExecRondeById(id);
        execRonde.setLastEventAt(LocalDateTime.now());
        return execRondeRepository.save(execRonde);
    }
}