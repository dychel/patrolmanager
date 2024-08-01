package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Unite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UniteRepository extends JpaRepository<Unite, Long> {
    @Query("select unite from Unite unite where unite.id = :id")
    Unite findByIdUnite(@Param("id") Long id);
}
