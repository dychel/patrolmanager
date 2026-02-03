package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.ProgRondeDTO;
import com.patrolmanagr.patrolmanagr.entity.*;
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
    public Prog_ronde saveProgRonde(ProgRondeDTO progRondeDTO) {
        Prog_ronde prog_ronde = modelMapper.map(progRondeDTO, Prog_ronde.class);
        prog_ronde.setCreated_by(userService.getConnectedUserId());

        // Mettre à jour les clés étrangères
        updateForeignKeys(progRondeDTO, prog_ronde);
        // Initialiser le statut si non fourni
        if (prog_ronde.getStatus() == null) {
            prog_ronde.setStatus(Status.ACTIVE);
        }

       return progRondeRepository.save(prog_ronde);
//        Prog_ronde progRonde = modelMapper.map(progRondeDTO, Prog_ronde.class);
//        progRonde.setCreated_by(userService.getConnectedUserId());
//        return progRondeRepository.save(progRonde);
    }

    @Override
    public Prog_ronde updateProgRonde(Long id, ProgRondeDTO progRondeDTO) {
        Prog_ronde progRondeToUpdate = progRondeRepository.findByIdProgRonde(id);
        if (progRondeToUpdate == null)
            throw new ApiRequestException("ProgRonde ID non trouvé");

        Prog_ronde prog_ronde = modelMapper.map(progRondeDTO, Prog_ronde.class);
        prog_ronde.setId(id);
        prog_ronde.setUpdated_at(LocalDateTime.now());
        prog_ronde.setUpdated_by(userService.getConnectedUserId());

        // Mettre à jour les clés étrangères
        updateForeignKeys(progRondeDTO, prog_ronde);

        return progRondeRepository.save(prog_ronde);
    }

    private void updateForeignKeys(ProgRondeDTO progRondeDTO, Prog_ronde prog_ronde) {
        // Mettre à jour id Ronde
        if (progRondeDTO.getRefRondeId() != null) {
            prog_ronde.setRef_ronde(refRondeService.findRondeById(progRondeDTO.getRefRondeId()));
        }

        // Mettre à jour id Site
        if (progRondeDTO.getRefSiteId() != null) {
            prog_ronde.setRef_site(refSiteService.findSiteById(progRondeDTO.getRefSiteId()));
        }

        // Mettre à jour id Agent (User)
        if (progRondeDTO.getAssignedAgentId() != null) {
            // Ici, vous auriez besoin d'un UserService pour récupérer l'utilisateur
             User agent = userService.getUserById(progRondeDTO.getAssignedAgentId());
             prog_ronde.setAssigned_agent_id(agent);
            // Pour l'instant, on laisse null ou on gère selon votre implémentation
        }

        // Mettre à jour id Terminal
        if (progRondeDTO.getAssignedRondierTerminalId() != null) {
            Ref_terminal terminal = refTerminalService.findTerminalById(progRondeDTO.getAssignedRondierTerminalId());
            prog_ronde.setAssigned_rondier_terminal_id(terminal);
        }
    }

    @Override
    public Prog_ronde findProgRondeById(Long id) {
        Prog_ronde progRondeToUpdate = progRondeRepository.findByIdProgRonde(id);
        if (progRondeToUpdate == null)
            throw new ApiRequestException("ProgRonde non trouvé");
        return progRondeToUpdate;
    }

    @Override
    public List<Prog_ronde> listProgRonde() {
        List<Prog_ronde> Prog_rondes = progRondeRepository.findAll();
        if (Prog_rondes.isEmpty())
            throw new ApiRequestException("Pas de programmations de ronde enregistrées dans la base de données");
        return Prog_rondes;
    }

    @Override
    public List<Prog_ronde> findProgRondeByRondeId(Long rondeId) {
        refRondeService.findRondeById(rondeId); // Vérifie si la ronde existe
        List<Prog_ronde> progRondes = progRondeRepository.findByRondeId(rondeId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour cette ronde");
        return progRondes;
    }

    @Override
    public List<Prog_ronde> findProgRondeBySiteId(Long siteId) {
        refSiteService.findSiteById(siteId); // Vérifie si le site existe
        List<Prog_ronde> progRondes = progRondeRepository.findBySiteId(siteId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour ce site");
        return progRondes;
    }

    @Override
    public List<Prog_ronde> findProgRondeByUserId(Long userId) {
        List<Prog_ronde> progRondes = progRondeRepository.findByUserId(userId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour cet utilisateur");
        return progRondes;
    }

    @Override
    public List<Prog_ronde> findProgRondeByStatus(Status status) {
        List<Prog_ronde> progRondes = progRondeRepository.findByStatus(status);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée avec ce statut");
        return progRondes;
    }

    @Override
    public List<Prog_ronde> findProgRondeByTerminalId(Long terminalId) {
        refTerminalService.findTerminalById(terminalId); // Vérifie si le terminal existe
        List<Prog_ronde> progRondes = progRondeRepository.findByTerminalId(terminalId);
        if (progRondes.isEmpty())
            throw new ApiRequestException("Pas de programmation trouvée pour ce terminal");
        return progRondes;
    }

    @Override
    public void deleteProgRondeById(Long id) {
        Prog_ronde prog_ronde = progRondeRepository.findByIdProgRonde(id);
        if (prog_ronde == null)
            throw new ApiRequestException("ProgRonde non trouvé");
        progRondeRepository.deleteById(id);
    }
}