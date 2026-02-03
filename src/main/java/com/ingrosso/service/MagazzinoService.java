package com.ingrosso.service;

import com.ingrosso.dao.GiacenzaDao;
import com.ingrosso.dao.LottoDao;
import com.ingrosso.dao.MagazzinoDao;
import com.ingrosso.model.Giacenza;
import com.ingrosso.model.Lotto;
import com.ingrosso.model.Magazzino;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class MagazzinoService {
    private static final Logger logger = LoggerFactory.getLogger(MagazzinoService.class);
    private static MagazzinoService instance;

    private final MagazzinoDao magazzinoDao;
    private final GiacenzaDao giacenzaDao;
    private final LottoDao lottoDao;

    private MagazzinoService() {
        this.magazzinoDao = new MagazzinoDao();
        this.giacenzaDao = new GiacenzaDao();
        this.lottoDao = new LottoDao();
    }

    public static synchronized MagazzinoService getInstance() {
        if (instance == null) {
            instance = new MagazzinoService();
        }
        return instance;
    }

    // Magazzini
    public List<Magazzino> getAllMagazzini() {
        return magazzinoDao.findAllActive();
    }

    public Optional<Magazzino> getMagazzinoById(int id) {
        return magazzinoDao.findById(id);
    }

    public Optional<Magazzino> getMagazzinoPrincipale() {
        return magazzinoDao.findPrincipale();
    }

    public int saveMagazzino(Magazzino magazzino) {
        if (magazzino.getId() > 0) {
            return magazzinoDao.update(magazzino) ? magazzino.getId() : -1;
        } else {
            return magazzinoDao.insert(magazzino);
        }
    }

    public boolean deleteMagazzino(int id) {
        Optional<Magazzino> magOpt = magazzinoDao.findById(id);
        if (magOpt.isPresent()) {
            Magazzino m = magOpt.get();
            m.setAttivo(false);
            return magazzinoDao.update(m);
        }
        return false;
    }

    public String generateMagazzinoCodice() {
        return magazzinoDao.generateNextCode();
    }

    // Giacenze
    public List<GiacenzaDao.GiacenzaCompleta> getGiacenzeComplete(int magazzinoId) {
        return giacenzaDao.findGiacenzeComplete(magazzinoId);
    }

    public BigDecimal getGiacenza(int prodottoId, int magazzinoId) {
        Optional<Giacenza> giacenza = giacenzaDao.findByProdottoAndMagazzino(prodottoId, magazzinoId);
        return giacenza.map(Giacenza::getQuantita).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getGiacenzaTotale(int prodottoId) {
        return giacenzaDao.getGiacenzaTotale(prodottoId);
    }

    public boolean updateGiacenza(int prodottoId, int magazzinoId, BigDecimal quantita) {
        return giacenzaDao.updateQuantita(prodottoId, magazzinoId, quantita);
    }

    // Lotti
    public List<Lotto> getLottiByProdotto(int prodottoId, int magazzinoId) {
        return lottoDao.findByProdottoAndMagazzino(prodottoId, magazzinoId);
    }

    public List<LottoDao.LottoCompleto> getLottiInScadenza(int giorni) {
        return lottoDao.findLottiInScadenzaCompleti(giorni);
    }

    public Optional<Lotto> getLottoById(int id) {
        return lottoDao.findById(id);
    }

    public int saveLotto(Lotto lotto) {
        if (lotto.getId() > 0) {
            return lottoDao.update(lotto) ? lotto.getId() : -1;
        } else {
            return lottoDao.insert(lotto);
        }
    }

    public boolean deleteLotto(int id) {
        return lottoDao.deleteById(id);
    }

    // Statistiche
    public BigDecimal getValoreMagazzino(int magazzinoId, boolean prezzoVendita) {
        List<GiacenzaDao.GiacenzaCompleta> giacenze = giacenzaDao.findGiacenzeComplete(magazzinoId);
        return giacenze.stream()
                .map(g -> prezzoVendita ? g.getValoreVendita() : g.getValoreAcquisto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValoreMagazzinoTotale(boolean prezzoVendita) {
        List<Magazzino> magazzini = magazzinoDao.findAllActive();
        BigDecimal totale = BigDecimal.ZERO;
        for (Magazzino m : magazzini) {
            totale = totale.add(getValoreMagazzino(m.getId(), prezzoVendita));
        }
        return totale;
    }

    public int countProdottiSottoScorta(int magazzinoId) {
        return (int) giacenzaDao.findGiacenzeComplete(magazzinoId).stream()
                .filter(GiacenzaDao.GiacenzaCompleta::isSottoScorta)
                .count();
    }

    public int countLottiInScadenza(int giorni) {
        return lottoDao.findLottiInScadenzaCompleti(giorni).size();
    }
}
