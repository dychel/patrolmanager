package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ref_siteDTO;
import com.patrolmanagr.patrolmanagr.dto.Ref_terminalDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_terminal;

import java.util.List;

public interface RefTerminalService {

    Ref_terminal saveTerminal(Ref_terminalDTO ref_terminalDTO);
    Ref_terminal updateTerminal(Long id, Ref_terminalDTO ref_terminalDTO);
    Ref_terminal findTerminalById(Long id);
    Ref_terminal findVendorById(Long id);
    List<Ref_terminal> listTerminal();
    Ref_terminal findTerminalByIdSite(Long id);
    Ref_terminal findTerminalByIdVendor(Long id);
    void deleteTerminalById(Long id);
}
