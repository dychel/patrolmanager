package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.dto.Ref_vendor_apiDTO;
import com.patrolmanagr.patrolmanagr.entity.Ref_vendor_api;
import java.util.List;

public interface RefVendorApiService {

    Ref_vendor_api saveVendor(Ref_vendor_apiDTO refVendorApiDTO);
    Ref_vendor_api updateVendor(Long id, Ref_vendor_apiDTO refVendorApiDTO);
    Ref_vendor_api findVendorById(Long id);
    List<Ref_vendor_api> listVendor();
    void deleteVendorById(Long id);
}
