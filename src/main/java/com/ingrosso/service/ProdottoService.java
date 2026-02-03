package com.ingrosso.service;

import com.ingrosso.dao.CategoriaDao;
import com.ingrosso.dao.GiacenzaDao;
import com.ingrosso.dao.ProdottoDao;
import com.ingrosso.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProdottoService {
    private static final Logger logger = LoggerFactory.getLogger(ProdottoService.class);
    private static ProdottoService instance;

    private final ProdottoDao prodottoDao;
    private final CategoriaDao categoriaDao;
    private final GiacenzaDao giacenzaDao;

    private ProdottoService() {
        this.prodottoDao = new ProdottoDao();
        this.categoriaDao = new CategoriaDao();
        this.giacenzaDao = new GiacenzaDao();
    }

    public static synchronized ProdottoService getInstance() {
        if (instance == null) {
            instance = new ProdottoService();
        }
        return instance;
    }

    // Prodotti
    public List<Prodotto> getAllProdotti() {
        List<Prodotto> prodotti = prodottoDao.findAllActive();
        enrichProdotti(prodotti);
        return prodotti;
    }

    public Optional<Prodotto> getProdottoById(int id) {
        Optional<Prodotto> prodottoOpt = prodottoDao.findById(id);
        prodottoOpt.ifPresent(p -> {
            enrichProdotto(p);
        });
        return prodottoOpt;
    }

    public Optional<Prodotto> getProdottoByCodice(String codice) {
        Optional<Prodotto> prodottoOpt = prodottoDao.findByCodice(codice);
        prodottoOpt.ifPresent(this::enrichProdotto);
        return prodottoOpt;
    }

    public Optional<Prodotto> getProdottoByBarcode(String barcode) {
        Optional<Prodotto> prodottoOpt = prodottoDao.findByBarcode(barcode);
        prodottoOpt.ifPresent(this::enrichProdotto);
        return prodottoOpt;
    }

    public List<Prodotto> searchProdotti(String text) {
        List<Prodotto> prodotti = prodottoDao.search(text);
        enrichProdotti(prodotti);
        return prodotti;
    }

    public List<Prodotto> getProdottiByCategoria(int categoriaId) {
        List<Prodotto> prodotti = prodottoDao.findByCategoria(categoriaId);
        enrichProdotti(prodotti);
        return prodotti;
    }

    public List<Prodotto> getProdottiBySottocategoria(int sottocategoriaId) {
        List<Prodotto> prodotti = prodottoDao.findBySottocategoria(sottocategoriaId);
        enrichProdotti(prodotti);
        return prodotti;
    }

    public List<Prodotto> getProdottiSottoScorta(int magazzinoId) {
        return prodottoDao.findSottoScorta(magazzinoId);
    }

    public int saveProdotto(Prodotto prodotto) {
        if (prodotto.getId() > 0) {
            return prodottoDao.update(prodotto) ? prodotto.getId() : -1;
        } else {
            return prodottoDao.insert(prodotto);
        }
    }

    public boolean deleteProdotto(int id) {
        // Soft delete - set attivo = false
        Optional<Prodotto> prodottoOpt = prodottoDao.findById(id);
        if (prodottoOpt.isPresent()) {
            Prodotto p = prodottoOpt.get();
            p.setAttivo(false);
            return prodottoDao.update(p);
        }
        return false;
    }

    public String generateProdottoCodice(String prefix) {
        return prodottoDao.generateNextCode(prefix);
    }

    private void enrichProdotti(List<Prodotto> prodotti) {
        for (Prodotto p : prodotti) {
            enrichProdotto(p);
        }
    }

    private void enrichProdotto(Prodotto p) {
        // Set giacenza totale
        BigDecimal giacenza = giacenzaDao.getGiacenzaTotale(p.getId());
        p.setGiacenzaTotale(giacenza);

        // Set unita misura
        if (p.getUnitaMisuraId() > 0) {
            UnitaMisura um = prodottoDao.findUnitaMisuraById(p.getUnitaMisuraId());
            p.setUnitaMisura(um);
        }

        // Set sottocategoria
        if (p.getSottocategoriaId() > 0) {
            Sottocategoria sc = categoriaDao.findSottocategoriaById(p.getSottocategoriaId());
            if (sc != null) {
                p.setSottocategoria(sc);
                // Set categoria
                categoriaDao.findById(sc.getCategoriaId()).ifPresent(sc::setCategoria);
            }
        }
    }

    // Categorie
    public List<Categoria> getAllCategorie() {
        return categoriaDao.findAllOrdered();
    }

    public List<Categoria> getCategorieActive() {
        return categoriaDao.findAllActive();
    }

    public int saveCategoria(Categoria categoria) {
        if (categoria.getId() > 0) {
            return categoriaDao.update(categoria) ? categoria.getId() : -1;
        } else {
            return categoriaDao.insert(categoria);
        }
    }

    public boolean deleteCategoria(int id) {
        return categoriaDao.deleteById(id);
    }

    // Sottocategorie
    public List<Sottocategoria> getSottocategorieByCategoria(int categoriaId) {
        return categoriaDao.findSottocategorieByCategoria(categoriaId);
    }

    public int saveSottocategoria(Sottocategoria sottocategoria) {
        if (sottocategoria.getId() > 0) {
            return categoriaDao.updateSottocategoria(sottocategoria) ? sottocategoria.getId() : -1;
        } else {
            return categoriaDao.insertSottocategoria(sottocategoria);
        }
    }

    public boolean deleteSottocategoria(int id) {
        return categoriaDao.deleteSottocategoria(id);
    }

    // Unita Misura
    public List<UnitaMisura> getAllUnitaMisura() {
        return prodottoDao.findAllUnitaMisura();
    }

    public int saveUnitaMisura(UnitaMisura um) {
        if (um.getId() > 0) {
            return prodottoDao.updateUnitaMisura(um) ? um.getId() : -1;
        } else {
            return prodottoDao.insertUnitaMisura(um);
        }
    }

    // Stats
    public long countProdottiAttivi() {
        return prodottoDao.findAllActive().size();
    }
}
