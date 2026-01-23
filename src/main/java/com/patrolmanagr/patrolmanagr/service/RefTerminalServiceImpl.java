package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ref_siteDTO;
import com.patrolmanagr.patrolmanagr.dto.Ref_terminalDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_terminal;
import com.patrolmanagr.patrolmanagr.entity.Ref_vendor_api;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefSiteRepository;
import com.patrolmanagr.patrolmanagr.repository.RefTerminalRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefTerminalServiceImpl implements RefTerminalService{

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserService userService;
    @Autowired
    private RefTerminalRepository refTerminalRepository;
    @Autowired
    RefSiteService refSiteService;
    @Autowired
    RefVendorApiService refVendorApiService;
    @Override
    public Ref_terminal saveTerminal(Ref_terminalDTO ref_terminalDTO) {
        Ref_terminal ref_terminal = modelMapper.map(ref_terminalDTO, Ref_terminal.class);
        ref_terminal.setCreated_by(userService.getConnectedUserId());
        return refTerminalRepository.save(ref_terminal);
    }

    @Override
    public Ref_terminal updateTerminal(Long id, Ref_terminalDTO ref_terminalDTO) {
        Ref_terminal refTerminalToUpdate = refTerminalRepository.findByIdTerminal(id);
        if (refTerminalToUpdate == null)
            throw new ApiRequestException("Terminal ID non trouvé");
        Ref_terminal ref_terminal = modelMapper.map(ref_terminalDTO, Ref_terminal.class);
        ref_terminal.setId(ref_terminal.getId());
        ref_terminal.setUpdated_at(LocalDateTime.now());
        ref_terminal.setUpdated_by(userService.getConnectedUserId());
        // MAJ id zone
        updateForeignKeySite_VendorApi(ref_terminalDTO, ref_terminal);
        return refTerminalRepository.save(ref_terminal);
    }

    @Override
    public Ref_terminal findTerminalById(Long id) {
        Ref_terminal terminalToUpdate = refTerminalRepository.findByIdTerminal(id);
        if (terminalToUpdate == null)
            throw new ApiRequestException("Terminal non trouvé");
        return refTerminalRepository.findByIdTerminal(id);
    }

    private void updateForeignKeySite_VendorApi(Ref_terminalDTO ref_terminalDTO, Ref_terminal ref_terminal) {
        // mettre a jour id Site si pas null
        if (ref_terminalDTO.getSiteId() != null )
            ref_terminal.setRef_site(refSiteService.findSiteById(ref_terminalDTO.getSiteId()));
        if (ref_terminalDTO.getVendorId() != null )
            ref_terminal.setRef_vendor_id(refVendorApiService.findVendorById(ref_terminalDTO.getVendorId()));
    }

    @Override
    public Ref_terminal findVendorById(Long id) {
        return null;
    }

    @Override
    public List<Ref_terminal> listTerminal() {
        List<Ref_terminal> listTerminal = refTerminalRepository.findAll();
        if (listTerminal.isEmpty())
            throw new ApiRequestException("Pas de terminal enregister dans la base de donnees");
        return refTerminalRepository.findAll();
    }

    @Override
    public Ref_terminal findTerminalByIdSite(Long id) {
        Ref_site ref_site = refSiteService.findSiteById(id);
        if (ref_site==null)
            throw new ApiRequestException("Pas de terminal enregister dans la base de donnees");
        return refTerminalRepository.findRef_terminalBySite(id);
    }

    @Override
    public Ref_terminal findTerminalByIdVendor(Long id) {
        Ref_vendor_api ref_vendor_api = refVendorApiService.findVendorById(id);
        if (ref_vendor_api==null)
            throw new ApiRequestException("Pas de terminal enregister dans la base de donnees");
        return refTerminalRepository.findByIdVendor(id);
    }

    @Override
    public void deleteTerminalById(Long id) {
        Ref_terminal TerminalToUpdate = refTerminalRepository.findByIdTerminal(id);
        if (TerminalToUpdate == null)
            throw new ApiRequestException("Site non trouvé");
        refTerminalRepository.deleteById(id);
    }
}
