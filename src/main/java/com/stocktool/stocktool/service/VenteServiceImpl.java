package com.stocktool.stocktool.service;
import com.stocktool.stocktool.dto.DetailsVenteDTO;
import com.stocktool.stocktool.dto.StockDTO;
import com.stocktool.stocktool.dto.VenteDTO;
import com.stocktool.stocktool.entity.*;
import com.stocktool.stocktool.exception.ApiRequestException;
import com.stocktool.stocktool.repository.VenteRepository;
import com.stocktool.stocktool.response.ResponseMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class VenteServiceImpl implements VenteService{

    @Autowired
    VenteRepository venteRepository;
    @Autowired
    DetailsVenteService detailsVenteService;
    @Autowired
    StockService stockService;
    @Autowired
    EquipeService equipeService;
    @Autowired
    MenusService menusService;
    @Autowired
    UserService userService;
    @Autowired
    ProduitService produitService;
    @Autowired
    CompositionService compositionService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    RavitaillementService ravitaillementService;
    private List<Composition> listOfCompo;
    private double qte_vendue;
    private Long prod, ravitID;
    Long currentTimeInMillis = System.currentTimeMillis();
    Date currentDate;
    @Override
    public ResponseEntity<ResponseMessage> listVentes() {
        if (venteRepository.count()==0)
            return new ResponseEntity<ResponseMessage>(new ResponseMessage("error", "Pas de vente", null), HttpStatus.OK);
        return new ResponseEntity<ResponseMessage>(new ResponseMessage("ok", "Liste des ventes", venteRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public Vente saveVente(VenteDTO venteDTO, StockDTO stockDTO, DetailsVenteDTO detailsVenteDTO) {
        Vente vente = modelMapper.map(venteDTO, Vente.class);
        return getVenteSaved(venteDTO, vente, stockDTO, detailsVenteDTO);
    }

    @Override
    public Vente updateVente(Long id, VenteDTO venteDTO) {
        Vente venteUpdate = venteRepository.findByIdVente(id);

        if (venteUpdate == null)
            throw new ApiRequestException("Ravitaillement non effectué");
        Vente vente = modelMapper.map(venteDTO, Vente.class);
        vente.setId(venteUpdate.getId());
        // MAJ des tables unit, brand et Catégories liées à Product
        updateForeignKeyVente(venteDTO, vente);
        return venteRepository.save(vente);
    }

    private void updateStock(VenteDTO venteDTO , StockDTO stockDTO, DetailsVenteDTO detailsVenteDTO){
        //recuperation data
        currentDate = new Date(currentTimeInMillis);
        //Get la liste des produits concernant un menu
        listOfCompo = compositionService.getCompositionByMenu(venteDTO.getMenuId());
        //check if not empty
        if(listOfCompo!=null){
            for(Composition compo: listOfCompo){
                //get prod ID
                prod=compo.getProduit().getId();
                //Get qte_vendu by compo(produit)*total_vente
                qte_vendue = compo.getQuantite() * venteDTO.getTotal_vendu();
                //Get the stock prod
                Stock stock_update = stockService.findStockByProduit(prod);
                // si un id stock exist dans stock_update, on met à jour sinon on rajoute
                if(stock_update==null){
                    stockDTO.setQte_en_stock(stockDTO.getQte_en_stock());
                    stockDTO.setProduitId(prod);
                    stockDTO.setDatemaj(currentDate);
                    stockService.saveStock(stockDTO);
                    //add detail vente
                    addDetailsVentes(prod, venteDTO, detailsVenteDTO,stockDTO,qte_vendue);
                }else{
                    stockDTO.setQte_en_stock(stock_update.getQte_en_stock() - qte_vendue);
                    stockDTO.setProduitId(prod);
                    stockDTO.setDatemaj(currentDate);
                    //update the stock
                    stockService.updateStock(stock_update.getId(), stockDTO);
                    //add detail vente
                    addDetailsVentes(prod, venteDTO, detailsVenteDTO,stockDTO,qte_vendue);
                }
            }
        }
    }

    private void addDetailsVentes(Long idProd, VenteDTO venteDTO, DetailsVenteDTO detailsVenteDTO, StockDTO stockDTO, double qte_vendue){
        currentDate = new Date(currentTimeInMillis);

        Produit produit = produitService.findProduitById(idProd);
        Ravitaillement ravitaillement = ravitaillementService.getRavitByLastProd(produit.getId());
        //get details ventes datas
        detailsVenteDTO.setQte_av_vente(stockDTO.getQte_en_stock());
        detailsVenteDTO.setVenteId(venteDTO.getId());
        detailsVenteDTO.setProduitId(idProd);
        detailsVenteDTO.setMenuId(venteDTO.getMenuId());
        detailsVenteDTO.setDate(currentDate);
        detailsVenteDTO.setProduitId(stockDTO.getProduitId());
        detailsVenteDTO.setQte_restante(stockDTO.getQte_en_stock()-qte_vendue);
        detailsVenteDTO.setCout_unitaire(ravitaillement.getPrixuntaire());
        detailsVenteDTO.setTotal_menu_vendu(venteDTO.getTotal_vendu());
        detailsVenteDTO.setCout_total((float) (qte_vendue * ravitaillement.getPrixuntaire()));
        detailsVenteDTO.setBenefice(venteDTO.getPrix_de_vente()-ravitaillement.getPrixuntaire());
        //end getting datas

        //save all detail vente datas
        detailsVenteService.saveDetailVente(detailsVenteDTO);
    }
    private Vente getVenteSaved(VenteDTO venteDTO, Vente vente, StockDTO stockDTO, DetailsVenteDTO detailsVenteDTO) {
        currentDate = new Date(currentTimeInMillis);

        updateForeignKeyVente(venteDTO, vente);
        updateStock(venteDTO, stockDTO, detailsVenteDTO);
        vente.setPrix_de_vente(vente.getMenus().getPrix());
        vente.setDate(currentDate);
        vente.setPrix_total(venteDTO.getPrix_de_vente()*venteDTO.getTotal_vendu());
        return venteRepository.save(vente);
    }

    private void updateForeignKeyVente(VenteDTO venteDTO, Vente vente) {
        if (venteDTO.getEquipeId() != null)
            vente.setEquipe(equipeService.findEquipeById(venteDTO.getEquipeId()));
        if (venteDTO.getMenuId() != null)
            vente.setMenus(menusService.findMenusById(venteDTO.getMenuId()));
        if (venteDTO.getUserId() != null)
            vente.setUser(userService.getUserById(venteDTO.getUserId()));
    }

    @Override
    public Vente findVenteById(Long id) {
        Vente vente = venteRepository.findByIdVente(id);
        if (vente==null)
            throw new ApiRequestException("pas de vente correspondant");
        return venteRepository.findByIdVente(id);
    }

    @Override
    public List<Vente> getVenteByMenu(Long id) {
        return venteRepository.findVenteByMenu(id);
    }

    @Override
    public List<Vente> getVenteByUser(Long id) {
        return venteRepository.findVenteByUser(id);
    }

    @Override
    public List<Vente> getVenteByEquipe(Long id) {
        return venteRepository.findVenteByEquipe(id);
    }

    @Override
    public void deleteVenteById(Long id) {
        Vente vente = venteRepository.findByIdVente(id);
        if (vente==null){
            throw new ApiRequestException("Pas de vente correspondant!");
        }else{
            venteRepository.deleteById(id);
        }
    }
}
