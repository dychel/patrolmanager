package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_zoneDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import java.util.List;

public interface RefZoneService {
    Ref_zone saveZone(Ref_zoneDTO ref_zoneDTO);
    Ref_zone updateZone(Long id, Ref_zoneDTO ref_zoneDTO);
    Ref_zone findZoneById(Long id);
    List<Ref_zone> listZone();
    void deleteZoneById(Long id);
}
