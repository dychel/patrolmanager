package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.Ref_zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefZoneRepository extends JpaRepository <Ref_zone, Long>{

    @Query("select ref_zone from Ref_zone ref_zone where ref_zone.id = :id")
    Ref_zone findByIdZone(@Param("id") Long id);
    @Query("select ref_zone from Ref_zone ref_zone where ref_zone.name = :name")
    Ref_zone findByName(@Param("name") String name);
    @Query("select ref_zone from Ref_zone ref_zone where ref_zone.code = :code")
    Ref_zone findByCode(@Param("code") String code);
}
