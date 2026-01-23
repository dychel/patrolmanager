package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_vendor_apiDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_vendor_api;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.RefVendorApiRepository;
import com.patrolmanagr.patrolmanagr.repository.RefZoneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefVendorApiServiceImpl implements RefVendorApiService{

    @Autowired
    RefVendorApiRepository refVendorApiRepository;
    @Autowired
    UserService userService;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Ref_vendor_api saveVendor(Ref_vendor_apiDTO refVendorApiDTO) {
        Ref_vendor_api ref_vendor_api = modelMapper.map(refVendorApiDTO, Ref_vendor_api.class);
        ref_vendor_api.setCreated_by(userService.getConnectedUserId());
        return refVendorApiRepository.save(ref_vendor_api);
    }

    @Override
    public Ref_vendor_api updateVendor(Long id, Ref_vendor_apiDTO refVendorApiDTO) {
        Ref_vendor_api refvendorToUpdate = refVendorApiRepository.findById_vendor_api(id);
        if (refvendorToUpdate == null)
            throw new ApiRequestException("Vendor Api non trouvé");
        //enregister les nouvelle infos
        Ref_vendor_api ref_vendor_api = modelMapper.map(refVendorApiDTO, Ref_vendor_api.class);
        ref_vendor_api.setId(refvendorToUpdate.getId());
        ref_vendor_api.setUpdated_at(LocalDateTime.now());
        ref_vendor_api.setUpdated_by(userService.getConnectedUserId());
        return refVendorApiRepository.save(ref_vendor_api);
    }

    @Override
    public Ref_vendor_api findVendorById(Long id) {
        Ref_vendor_api ref_vendor_api = refVendorApiRepository.findById_vendor_api(id);
        if (ref_vendor_api == null)
            throw new ApiRequestException("Vendor api non trouvé");
        return ref_vendor_api;
    }

    @Override
    public List<Ref_vendor_api> listVendor() {
        List<Ref_vendor_api> list = refVendorApiRepository.findAll();
        if (list.isEmpty())
            throw new ApiRequestException("Pas de vendor enregister dans la base de donnees");
        return refVendorApiRepository.findAll();
    }

    @Override
    public void deleteVendorById(Long id) {
        Ref_vendor_api ref_vendor_api = refVendorApiRepository.findById_vendor_api(id);
        if (ref_vendor_api == null)
            throw new ApiRequestException("Zone non trouvé");
        refVendorApiRepository.deleteById(id);
    }
}
