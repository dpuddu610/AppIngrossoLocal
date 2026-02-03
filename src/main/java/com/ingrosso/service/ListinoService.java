package com.ingrosso.service;

import com.ingrosso.dao.ListinoDao;
import com.ingrosso.dao.ProdottoDao;
import com.ingrosso.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ListinoService {
    private static final Logger logger = LoggerFactory.getLogger(ListinoService.class);
    private static ListinoService instance;

    private final ListinoDao listinoDao;
    private final ProdottoDao prodottoDao;

    private ListinoService() {
        this.listinoDao = new ListinoDao();
        this.prodottoDao = new ProdottoDao();
    }

    public static synchronized ListinoService getInstance() {
        if (instance == null) {
            instance = new ListinoService();
        }
        return instance;
    }

    // Listini
    public List<Listino> getAllListini() {
        return listinoDao.findAllActive();
    }

    public List<Listino> getListiniByTipo(TipoListino tipo) {
        return listinoDao.findByTipo(tipo);
    }

    public Optional<Listino> getListinoById(int id) {
        Optional<Listino> listinoOpt = listinoDao.findById(id);
        listinoOpt.ifPresent(listino -> {
            List<ListinoPrezzo> prezzi = listinoDao.findPrezziByListino(id);
            prezzi.forEach(p -> {
                prodottoDao.findById(p.getProdottoId()).ifPresent(p::setProdotto);
            });
            listino.getPrezzi().setAll(prezzi);
        });
        return listinoOpt;
    }

    public Optional<Listino> getListinoPrincipale(TipoListino tipo) {
        return listinoDao.findPrincipale(tipo);
    }

    public int saveListino(Listino listino) {
        if (listino.getId() > 0) {
            return listinoDao.update(listino) ? listino.getId() : -1;
        } else {
            return listinoDao.insert(listino);
        }
    }

    public boolean deleteListino(int id) {
        Optional<Listino> listinoOpt = listinoDao.findById(id);
        if (listinoOpt.isPresent()) {
            Listino l = listinoOpt.get();
            l.setAttivo(false);
            return listinoDao.update(l);
        }
        return false;
    }

    // Prezzi
    public Optional<BigDecimal> getPrezzoCorrente(int listinoId, int prodottoId) {
        return listinoDao.findPrezzoCorrente(listinoId, prodottoId)
                .map(ListinoPrezzo::getPrezzo);
    }

    public BigDecimal getPrezzoVenditaProdotto(int prodottoId) {
        BigDecimal prezzo = listinoDao.getPrezzoVendita(prodottoId);
        if (prezzo == null) {
            // Fallback to product price
            Optional<Prodotto> prodotto = prodottoDao.findById(prodottoId);
            prezzo = prodotto.map(Prodotto::getPrezzoVendita).orElse(null);
        }
        return prezzo;
    }

    public int savePrezzo(ListinoPrezzo prezzo, Integer utenteId) {
        // Get old price for history
        Optional<ListinoPrezzo> oldPrezzoOpt = listinoDao.findPrezzoCorrente(
                prezzo.getListinoId(), prezzo.getProdottoId());

        int result;
        if (prezzo.getId() > 0) {
            result = listinoDao.updatePrezzo(prezzo) ? prezzo.getId() : -1;
        } else {
            result = listinoDao.insertPrezzo(prezzo);
        }

        // Record price history
        if (result > 0 && oldPrezzoOpt.isPresent()) {
            BigDecimal oldPrezzo = oldPrezzoOpt.get().getPrezzo();
            if (oldPrezzo.compareTo(prezzo.getPrezzo()) != 0) {
                Optional<Listino> listino = listinoDao.findById(prezzo.getListinoId());
                if (listino.isPresent()) {
                    StoricoPrezzo storico = new StoricoPrezzo(
                            prezzo.getProdottoId(),
                            listino.get().getTipo(),
                            oldPrezzo,
                            prezzo.getPrezzo()
                    );
                    if (utenteId != null) storico.setUtenteId(utenteId);
                    listinoDao.insertStoricoPrezzo(storico);
                }
            }
        }

        return result;
    }

    public boolean deletePrezzo(int prezzoId) {
        return listinoDao.deletePrezzo(prezzoId);
    }

    public void updatePrezziFromProduct(Prodotto prodotto, Integer utenteId) {
        // Update listino vendita principale
        Optional<Listino> listinoVendita = getListinoPrincipale(TipoListino.VENDITA);
        if (listinoVendita.isPresent() && prodotto.getPrezzoVendita() != null) {
            ListinoPrezzo prezzo = new ListinoPrezzo();
            prezzo.setListinoId(listinoVendita.get().getId());
            prezzo.setProdottoId(prodotto.getId());
            prezzo.setPrezzo(prodotto.getPrezzoVendita());
            prezzo.setDataInizio(LocalDate.now());
            savePrezzo(prezzo, utenteId);
        }

        // Update listino acquisto principale
        Optional<Listino> listinoAcquisto = getListinoPrincipale(TipoListino.ACQUISTO);
        if (listinoAcquisto.isPresent() && prodotto.getPrezzoAcquisto() != null) {
            ListinoPrezzo prezzo = new ListinoPrezzo();
            prezzo.setListinoId(listinoAcquisto.get().getId());
            prezzo.setProdottoId(prodotto.getId());
            prezzo.setPrezzo(prodotto.getPrezzoAcquisto());
            prezzo.setDataInizio(LocalDate.now());
            savePrezzo(prezzo, utenteId);
        }
    }

    // Storico
    public List<StoricoPrezzo> getStoricoPrezzi(int prodottoId) {
        return listinoDao.findStoricoPrezzi(prodottoId);
    }
}
