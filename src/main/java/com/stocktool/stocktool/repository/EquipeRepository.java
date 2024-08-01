package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    @Query("select equipe from Equipe equipe where equipe.id = :id")
    Equipe findByIdEquipe(@Param("id") Long id);
}
