package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.dto.Ref_clientDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_client;

import java.util.List;

public interface RefClientService {
    Ref_client saveClient(Ref_clientDTO ref_clientDTO);
    Ref_client updateClient(Long id, Ref_clientDTO ref_clientDTO);
    Ref_client findClientById(Long id);
    List<Ref_client> listClients();
    void deleteClientById(Long id);
}