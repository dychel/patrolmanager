package com.stocktool.stocktool.repository;


import com.stocktool.stocktool.entity.Marque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MarqueRepository extends JpaRepository<Marque, Long> {

    @Query("select marque from Marque marque where marque.id=:id")
    Marque findByIdMarque(@Param("id") Long id);
}
