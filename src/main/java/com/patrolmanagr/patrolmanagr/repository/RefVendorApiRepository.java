package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.Ref_vendor_api;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefVendorApiRepository extends JpaRepository<Ref_vendor_api, Long> {

    @Query("select ref_vendor_api from Ref_vendor_api ref_vendor_api where ref_vendor_api.id = :id")
    Ref_vendor_api findById_vendor_api(@Param("id") Long id);
    @Query("select ref_vendor_api from Ref_vendor_api ref_vendor_api where ref_vendor_api.name = :name")
    Ref_zone findByName(@Param("name") String name);
    @Query("select ref_vendor_api from Ref_vendor_api ref_vendor_api where ref_vendor_api.vendor_code = :vendor_code")
    Ref_zone findByCode(@Param("vendor_code") String vendor_code);
}
