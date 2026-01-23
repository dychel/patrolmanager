package com.patrolmanagr.patrolmanagr.repository;
import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

@Repository
public interface RefSiteRepository extends JpaRepository<Ref_site, Long> {

    @Query("select ref_site from Ref_site ref_site where ref_site.id = :id")
    Ref_site findByIdSite(@Param("id") Long id);
    @Query("select ref_site from Ref_site ref_site where ref_site.name = :name")
    Ref_site findByName(@Param("name") String name);
    @Query("select ref_site from Ref_site ref_site where ref_site.code = :code")
    Ref_site findByCode(@Param("code") String code);
    @Query("select ref_site from Ref_site ref_site where ref_site.ref_zone.id = :id")
    Ref_site findSiteByZone(@PathVariable("id") Long id);
}
