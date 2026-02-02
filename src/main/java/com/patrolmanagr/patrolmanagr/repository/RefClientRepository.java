package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Ref_client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefClientRepository extends JpaRepository<Ref_client, Long> {

    @Query("select c from Ref_client c where c.id = :id")
    Ref_client findByIdClient(@Param("id") Long id);

    @Query("select c from Ref_client c where c.name = :name")
    Ref_client findByName(@Param("name") String name);

    @Query("select c from Ref_client c where c.code = :code")
    Ref_client findByCode(@Param("code") String code);

    @Query("select c from Ref_client c where c.email = :email")
    Ref_client findByEmail(@Param("email") String email);
}