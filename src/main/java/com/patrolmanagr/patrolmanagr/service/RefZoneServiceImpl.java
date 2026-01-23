package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_zoneDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefZoneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefZoneServiceImpl implements RefZoneService{

    @Autowired
    RefZoneRepository refZoneRepository;
    @Autowired
    UserService userService;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Ref_zone saveZone(Ref_zoneDTO ref_zoneDTO) {
        Ref_zone ref_zone = modelMapper.map(ref_zoneDTO, Ref_zone.class);
        ref_zone.setCreated_by(userService.getConnectedUserId());
        return refZoneRepository.save(ref_zone);
    }

    @Override
    public Ref_zone updateZone(Long id, Ref_zoneDTO refZoneDTO) {
        Ref_zone zoneToUpdate = refZoneRepository.findByIdZone(id);
        if (zoneToUpdate == null)
            throw new ApiRequestException("Acces non trouvé");
        //enregister les nouvelle infos
        Ref_zone zone = modelMapper.map(refZoneDTO, Ref_zone.class);
        zone.setId(zoneToUpdate.getId());
        zone.setUpdated_at(LocalDateTime.now());
        zone.setUpdated_by(userService.getConnectedUserId());
        return refZoneRepository.save(zone);
    }

    @Override
    public Ref_zone findZoneById(Long id) {
        Ref_zone zoneToUpdate = refZoneRepository.findByIdZone(id);
        if (zoneToUpdate == null)
            throw new ApiRequestException("Zone non trouvé");
        return zoneToUpdate;
    }

    @Override
    public List<Ref_zone> listZone() {
        List<Ref_zone> listZone = refZoneRepository.findAll();
        if (listZone.isEmpty())
            throw new ApiRequestException("Pas de zone enregister dans la base de donnees");
        return refZoneRepository.findAll();
    }

    @Override
    public void deleteZoneById(Long id) {
        Ref_zone zoneToUpdate = refZoneRepository.findByIdZone(id);
        if (zoneToUpdate == null)
            throw new ApiRequestException("Zone non trouvé");
        refZoneRepository.deleteById(id);
    }
}
