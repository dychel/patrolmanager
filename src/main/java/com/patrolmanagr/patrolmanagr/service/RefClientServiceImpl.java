package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.Status;
import com.patrolmanagr.patrolmanagr.dto.Ref_clientDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_client;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefClientRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefClientServiceImpl implements RefClientService {

    @Autowired
    RefClientRepository refClientRepository;

    @Autowired
    UserService userService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Ref_client saveClient(Ref_clientDTO ref_clientDTO) {
        Ref_client ref_client = modelMapper.map(ref_clientDTO, Ref_client.class);
        ref_client.setCreated_by(userService.getConnectedUserId());

        // Initialiser le statut si non fourni
        if (ref_client.getStatus() == null) {
            ref_client.setStatus(Status.ACTIVE);
        }

        return refClientRepository.save(ref_client);
    }

    @Override
    public Ref_client updateClient(Long id, Ref_clientDTO ref_clientDTO) {
        Ref_client clientToUpdate = refClientRepository.findByIdClient(id);
        if (clientToUpdate == null)
            throw new ApiRequestException("Client non trouvé");

        Ref_client client = modelMapper.map(ref_clientDTO, Ref_client.class);
        client.setId(clientToUpdate.getId());
        client.setUpdated_at(LocalDateTime.now());
        client.setUpdated_by(userService.getConnectedUserId());

        return refClientRepository.save(client);
    }

    @Override
    public Ref_client findClientById(Long id) {
        Ref_client client = refClientRepository.findByIdClient(id);
        if (client == null)
            throw new ApiRequestException("Client non trouvé");
        return client;
    }

    @Override
    public List<Ref_client> listClients() {
        List<Ref_client> listClient = refClientRepository.findAll();
        if (listClient.isEmpty())
            throw new ApiRequestException("Pas de client enregistré dans la base de données");
        return listClient;
    }

    @Override
    public void deleteClientById(Long id) {
        Ref_client client = refClientRepository.findByIdClient(id);
        if (client == null)
            throw new ApiRequestException("Client non trouvé");
        refClientRepository.deleteById(id);
    }
}