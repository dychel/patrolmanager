package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("select stock from Stock stock where stock.id= :id")
    Stock findByIdStock(@Param("id") Long id);
    @Query("select stock from Stock stock where stock.produit.id=:id")
    Stock findStockByProduit(@PathVariable("id") Long id);
}
