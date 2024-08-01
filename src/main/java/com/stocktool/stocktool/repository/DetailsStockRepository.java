package com.stocktool.stocktool.repository;
import com.stocktool.stocktool.entity.DetailsVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Repository
public interface DetailsStockRepository extends JpaRepository<DetailsVente, Long> {

    @Query("select detailsvente from DetailsVente detailsvente where detailsvente.id= :id")
    DetailsVente findByIdDetailsVente(@Param("id") Long id);
    @Query("select detailsvente from DetailsVente detailsvente where detailsvente.produit.id=:id")
    List<DetailsVente> findDetailsVenteByProduit(@PathVariable("id") Long id);
    @Query("select detailsvente from DetailsVente detailsvente where detailsvente.menus.id=:id")
    List<DetailsVente> findDetailsVenteByMenu(@PathVariable("id") Long id);
    @Query("select detailsvente from DetailsVente detailsvente where detailsvente.vente.id=:id")
    List<DetailsVente> findDetailsVenteByVente(@PathVariable("id") Long id);
}
