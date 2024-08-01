package com.stocktool.stocktool.repository;

import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.entity.Ravitaillement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Repository
public interface RavitaillementRepository extends JpaRepository<Ravitaillement, Long> {
    Ravitaillement findByLibelle(String Libelle);
    @Query("select ravitaillement from Ravitaillement ravitaillement where ravitaillement.id= :id")
    Ravitaillement findByIdRavitaillement(@Param("id") Long id);

    @Query("select ravit from Ravitaillement ravit where ravit.produit.id=:id")
    Ravitaillement getLastQteById(@Param("id") Long id);
    @Query("select ravit from Ravitaillement ravit where ravit.produit.id=:id")
    List<Ravitaillement> findRavitaillementByProduit(@PathVariable("id") Long id);
    Ravitaillement findRavitaillementById(@PathVariable("id") Long id);
    @Query("select ravit from Ravitaillement ravit where ravit.fournisseur.id=:id")
    List<Ravitaillement> findProduitByFournisseur(@PathVariable("id") Long id);

}
