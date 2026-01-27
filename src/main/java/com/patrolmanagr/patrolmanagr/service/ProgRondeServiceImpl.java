package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.ProgRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.prog_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_ronde;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_terminal;
import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.ProgRondeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProgRondeServiceImpl implements ProgRondeService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserService userService;

    @Autowired
    ProgRondeRepository progRondeRepository;
    @Autowired
    RefRondeService refRondeService;
    @Autowired
    RefSiteService refSiteService;
    @Autowired
    RefTerminalService refTerminalService;

    @Override
    public prog_ronde saveProgRonde(ProgRondeDTO progRondeDTO) {
        prog_ronde prog_ronde = modelMapper.map(progRondeDTO, prog_ronde.class);
        prog_ronde.setCreated_by(userService.getConnectedUserId());

        // Mettre à jour les clés étrangères
        updateForeignKeys(progRondeDTO, prog_ronde);

        // Initialiser le statut si non fourni
        if (prog_ronde.getStatus() == null) {
            prog_ronde.setStatus(Status.ACTIVE);
        }

        return progRondeRepository.save(prog_ronde);
    }

    @Override
    public prog_ronde updateProgRonde(Long id, ProgRondeDTO progRondeDTO) {
        prog_ronde progRondeToUpdate = progRondeRepository.findByIdProgRonde(id);
        if (progRondeToUpdate == null)
            throw new ApiRequestException("ProgRonde ID non trouvé");

        prog_ronde prog_ronde = modelMapper.map(progRondeDTO, prog_ronde.class);
        prog_ronde.setId(id);
        prog_ronde.setUpdated_at(LocalDateTime.now());
        prog_ronde.setUpdated_by(userService.getConnectedUserId());

        // Mettre à jour les clés étrangères
        updateForeignKeys(progRondeDTO, prog_ronde);

        return progRondeRepository.save(prog_ronde);
    }

    private void updateForeignKeys(ProgRondeDTO progRondeDTO, prog_ronde prog_ronde) {
        // Mettre à jour id Ronde
        if (progRondeDTO.getRefRondeId() != null) {
            Ref_ronde ronde = refRondeService.findRondeById(progRondeDTO.getRefRondeId());
            prog_ronde.setRef_ronde_id(ronde);
        }

        // Mettre à jour id Site
        if (progRondeDTO.getRefSiteId() != null) {
            Ref_site site = refSiteService.findSiteById(progRondeDTO.getRefSiteId());
            prog_ronde.setRef_site_id(site);
        }

        // Mettre à jour id Agent (User)
        if (progRondeDTO.getAssignedAgentId() != null) {
            // Ici, vous auriez besoin d'un UserService pour récupérer l'utilisateur
            // User agent = userService.findById(progRondeDTO.getAssignedAgentId());
            // prog_ronde.setAssigned_agent_id(agent);
            // Pour l'instant, on laisse null ou on gère selon votre implémentation
        }

        // Mettre à jour id Terminal
        if (progRondeDTO.getAssignedRondierTerminalId() != null) {
            Ref_terminal terminal = refTerminalService.findTerminalById(progRondeDTO.getAssignedRondierTerminalId());
            prog_ronde.setAssigned_rondier_terminal_id(terminal);
        }
    }

    @Override
    public prog_ronde findProgRondeById(Long id) {
        prog_ronde progRondeToUpdate = progRondeRepository.findByIdProgRonde(id);
        if (progRondeToUpdate == null)
            throw new ApiRequestException("ProgRonde non trouvé");
        return progRondeToUpdate;
    }

    @Override
    public List<prog_ronde> listProgRonde() {
        List<prog_ronde> prog_rondes = progRondeRepository.findAll();
        if (prog_rondes.isEmpty())
            throw new ApiRequestException("Pas de programmations de ronde enregistrées dans la base de données");
        return prog_rondes;
    }

    @Override
    public List<prog_ronde> findProgRondeByRondeId(Long rondeId) {
        refRondeService.findRondeById(rondeId); // Vérifie si la ronde existe
        List<prog_ronde> progRondes = progRondeRepository.findByRondeId(rondeId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour cette ronde");
        return progRondes;
    }

    @Override
    public List<prog_ronde> findProgRondeBySiteId(Long siteId) {
        refSiteService.findSiteById(siteId); // Vérifie si le site existe
        List<prog_ronde> progRondes = progRondeRepository.findBySiteId(siteId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour ce site");
        return progRondes;
    }

    @Override
    public List<prog_ronde> findProgRondeByUserId(Long userId) {
        List<prog_ronde> progRondes = progRondeRepository.findByUserId(userId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour cet utilisateur");
        return progRondes;
    }

    @Override
    public List<prog_ronde> findProgRondeByStatus(Status status) {
        List<prog_ronde> progRondes = progRondeRepository.findByStatus(status);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée avec ce statut");
        return progRondes;
    }

    @Override
    public List<prog_ronde> findProgRondeByTerminalId(Long terminalId) {
        refTerminalService.findTerminalById(terminalId); // Vérifie si le terminal existe
        List<prog_ronde> progRondes = progRondeRepository.findByTerminalId(terminalId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour ce terminal");
        return progRondes;
    }

    @Override
    public void deleteProgRondeById(Long id) {
        prog_ronde prog_ronde = progRondeRepository.findByIdProgRonde(id);
        if (prog_ronde == null)
            throw new ApiRequestException("ProgRonde non trouvé");
        progRondeRepository.deleteById(id);
    }
}