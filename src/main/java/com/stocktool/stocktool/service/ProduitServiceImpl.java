package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.ProduitDTO;
import com.stocktool.stocktool.entity.Produit;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.ProduitRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProduitServiceImpl implements ProduitService{

    @Autowired
    ProduitRepository produitRepository;
    @Autowired
    private UniteService uniteService;
    @Autowired
    private MarqueService marqueService;
    @Autowired
    private CategorieService categorieService;
    @Autowired
    private ModelMapper modelMapper;

@Override
public Produit saveProduit(ProduitDTO productDTO) {
    Produit produit = modelMapper.map(productDTO, Produit.class);
    return getProduitSaved(productDTO, produit);
}

@Override
public Produit updateProduit(Long id, ProduitDTO productDTO) {

    Produit produitToUpdate = produitRepository.findByIdProduit(id);

    if (produitToUpdate == null)
        throw new ApiRequestException("Produit non disponible");

    Produit produit = modelMapper.map(productDTO, Produit.class);

    produit.setId(produitToUpdate.getId());

    // MAJ des tables unit, brand et Catégories liées à Product
    updateForeignKeyProduit(productDTO, produit);

    return produitRepository.save(produit);
}

    private Produit getProduitSaved(ProduitDTO productDTO, Produit product) {
        updateForeignKeyProduit(productDTO, product);
        return produitRepository.save(product);
    }

    private void updateForeignKeyProduit(ProduitDTO productDTO, Produit product) {
        if (productDTO.getUniteId() != null)
            product.setUnite(uniteService.findUniteById(productDTO.getUniteId()));

        if (productDTO.getMarqueId() != null)
            product.setMarque(marqueService.findMarqueById(productDTO.getMarqueId()));

        if (productDTO.getCategorieId() != null)
            product.setCategorie(categorieService.findCategorieById(productDTO.getCategorieId()));
    }

    @Override
    public Produit findProduitById(Long id) {
        Produit produit = produitRepository.findByIdProduit(id);
        if (produit==null)
            throw new ApiRequestException("Produit non disponible");
        return produitRepository.findByIdProduit(id);
    }

    @Override
    public ResponseEntity<ResponseMessage> listProduits() {
        if (produitRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "Pas de produit disponible", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des produits", produitRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public void deleteProduitById(Long id) {
        Produit produit = produitRepository.findByIdProduit(id);
        if (produit==null){
            throw new ApiRequestException("Produit non disponible");
        }else{
            produitRepository.deleteById(id);
        }
    }

    @Override
    public List<Produit> getProduitByUnit(Long id) {
        return produitRepository.findProduitByUnite(id);
    }

    @Override
    public List<Produit> getProduitByMarque(Long id) {
        return produitRepository.findProduitByMarque(id);
    }

    @Override
    public List<Produit> getProduitByCategories(Long id) {
        return produitRepository.findProduitByCategorie(id);
    }
}

