package com.ingrosso.service;

import com.ingrosso.dao.GiacenzaDao;
import com.ingrosso.dao.LottoDao;
import com.ingrosso.dao.MovimentoDao;
import com.ingrosso.model.Movimento;
import com.ingrosso.model.TipoMovimento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MovimentoService {
    private static final Logger logger = LoggerFactory.getLogger(MovimentoService.class);
    private static MovimentoService instance;

    private final MovimentoDao movimentoDao;
    private final GiacenzaDao giacenzaDao;
    private final LottoDao lottoDao;

    private MovimentoService() {
        this.movimentoDao = new MovimentoDao();
        this.giacenzaDao = new GiacenzaDao();
        this.lottoDao = new LottoDao();
    }

    public static synchronized MovimentoService getInstance() {
        if (instance == null) {
            instance = new MovimentoService();
        }
        return instance;
    }

    public List<MovimentoDao.MovimentoCompleto> getMovimenti(LocalDate dataInizio, LocalDate dataFine,
                                                             Integer magazzinoId, TipoMovimento tipo) {
        return movimentoDao.findMovimentiCompleti(dataInizio, dataFine, magazzinoId, tipo);
    }

    public List<Movimento> getMovimentiByProdotto(int prodottoId) {
        return movimentoDao.findByProdotto(prodottoId);
    }

    public List<Movimento> getMovimentiRecenti(int limit) {
        return movimentoDao.findRecent(limit);
    }

    public Optional<Movimento> getMovimentoById(int id) {
        return movimentoDao.findById(id);
    }

    public int registraCarico(int prodottoId, int magazzinoId, BigDecimal quantita,
                              String causale, String documentoRif, Integer lottoId, Integer utenteId) {
        BigDecimal giacenzaAttuale = giacenzaDao.findByProdottoAndMagazzino(prodottoId, magazzinoId)
                .map(g -> g.getQuantita())
                .orElse(BigDecimal.ZERO);

        BigDecimal nuovaGiacenza = giacenzaAttuale.add(quantita);

        Movimento m = new Movimento();
        m.setProdottoId(prodottoId);
        m.setMagazzinoId(magazzinoId);
        m.setTipo(TipoMovimento.CARICO);
        m.setQuantita(quantita);
        m.setQuantitaPrecedente(giacenzaAttuale);
        m.setQuantitaSuccessiva(nuovaGiacenza);
        m.setCausale(causale);
        m.setDocumentoRif(documentoRif);
        if (lottoId != null && lottoId > 0) m.setLottoId(lottoId);
        if (utenteId != null && utenteId > 0) m.setUtenteId(utenteId);

        int movId = movimentoDao.insert(m);
        if (movId > 0) {
            giacenzaDao.updateQuantita(prodottoId, magazzinoId, nuovaGiacenza);
            if (lottoId != null && lottoId > 0) {
                lottoDao.incrementQuantita(lottoId, quantita);
            }
            logger.info("Carico registrato: prodotto={}, magazzino={}, quantita={}", prodottoId, magazzinoId, quantita);
        }
        return movId;
    }

    public int registraScarico(int prodottoId, int magazzinoId, BigDecimal quantita,
                               String causale, String documentoRif, Integer lottoId, Integer utenteId) {
        BigDecimal giacenzaAttuale = giacenzaDao.findByProdottoAndMagazzino(prodottoId, magazzinoId)
                .map(g -> g.getQuantita())
                .orElse(BigDecimal.ZERO);

        if (giacenzaAttuale.compareTo(quantita) < 0) {
            logger.warn("Giacenza insufficiente per scarico: disponibile={}, richiesta={}", giacenzaAttuale, quantita);
            return -1;
        }

        BigDecimal nuovaGiacenza = giacenzaAttuale.subtract(quantita);

        Movimento m = new Movimento();
        m.setProdottoId(prodottoId);
        m.setMagazzinoId(magazzinoId);
        m.setTipo(TipoMovimento.SCARICO);
        m.setQuantita(quantita);
        m.setQuantitaPrecedente(giacenzaAttuale);
        m.setQuantitaSuccessiva(nuovaGiacenza);
        m.setCausale(causale);
        m.setDocumentoRif(documentoRif);
        if (lottoId != null && lottoId > 0) m.setLottoId(lottoId);
        if (utenteId != null && utenteId > 0) m.setUtenteId(utenteId);

        int movId = movimentoDao.insert(m);
        if (movId > 0) {
            giacenzaDao.updateQuantita(prodottoId, magazzinoId, nuovaGiacenza);
            if (lottoId != null && lottoId > 0) {
                lottoDao.incrementQuantita(lottoId, quantita.negate());
            }
            logger.info("Scarico registrato: prodotto={}, magazzino={}, quantita={}", prodottoId, magazzinoId, quantita);
        }
        return movId;
    }

    public int registraRettifica(int prodottoId, int magazzinoId, BigDecimal nuovaQuantita,
                                 String causale, Integer utenteId) {
        BigDecimal giacenzaAttuale = giacenzaDao.findByProdottoAndMagazzino(prodottoId, magazzinoId)
                .map(g -> g.getQuantita())
                .orElse(BigDecimal.ZERO);

        BigDecimal differenza = nuovaQuantita.subtract(giacenzaAttuale);

        Movimento m = new Movimento();
        m.setProdottoId(prodottoId);
        m.setMagazzinoId(magazzinoId);
        m.setTipo(TipoMovimento.RETTIFICA);
        m.setQuantita(differenza);
        m.setQuantitaPrecedente(giacenzaAttuale);
        m.setQuantitaSuccessiva(nuovaQuantita);
        m.setCausale(causale != null ? causale : "Rettifica inventario");
        if (utenteId != null && utenteId > 0) m.setUtenteId(utenteId);

        int movId = movimentoDao.insert(m);
        if (movId > 0) {
            giacenzaDao.updateQuantita(prodottoId, magazzinoId, nuovaQuantita);
            logger.info("Rettifica registrata: prodotto={}, magazzino={}, da {} a {}",
                    prodottoId, magazzinoId, giacenzaAttuale, nuovaQuantita);
        }
        return movId;
    }

    public int registraTrasferimento(int prodottoId, int magazzinoOrigineId, int magazzinoDestinazioneId,
                                     BigDecimal quantita, String causale, Integer lottoId, Integer utenteId) {
        BigDecimal giacenzaOrigine = giacenzaDao.findByProdottoAndMagazzino(prodottoId, magazzinoOrigineId)
                .map(g -> g.getQuantita())
                .orElse(BigDecimal.ZERO);

        if (giacenzaOrigine.compareTo(quantita) < 0) {
            logger.warn("Giacenza insufficiente per trasferimento: disponibile={}, richiesta={}", giacenzaOrigine, quantita);
            return -1;
        }

        BigDecimal giacenzaDestinazione = giacenzaDao.findByProdottoAndMagazzino(prodottoId, magazzinoDestinazioneId)
                .map(g -> g.getQuantita())
                .orElse(BigDecimal.ZERO);

        Movimento m = new Movimento();
        m.setProdottoId(prodottoId);
        m.setMagazzinoId(magazzinoOrigineId);
        m.setMagazzinoDestinazioneId(magazzinoDestinazioneId);
        m.setTipo(TipoMovimento.TRASFERIMENTO);
        m.setQuantita(quantita);
        m.setQuantitaPrecedente(giacenzaOrigine);
        m.setQuantitaSuccessiva(giacenzaOrigine.subtract(quantita));
        m.setCausale(causale != null ? causale : "Trasferimento tra magazzini");
        if (lottoId != null && lottoId > 0) m.setLottoId(lottoId);
        if (utenteId != null && utenteId > 0) m.setUtenteId(utenteId);

        int movId = movimentoDao.insert(m);
        if (movId > 0) {
            giacenzaDao.updateQuantita(prodottoId, magazzinoOrigineId, giacenzaOrigine.subtract(quantita));
            giacenzaDao.updateQuantita(prodottoId, magazzinoDestinazioneId, giacenzaDestinazione.add(quantita));
            logger.info("Trasferimento registrato: prodotto={}, da {} a {}, quantita={}",
                    prodottoId, magazzinoOrigineId, magazzinoDestinazioneId, quantita);
        }
        return movId;
    }

    public int countMovimentiPeriodo(LocalDate dataInizio, LocalDate dataFine) {
        return movimentoDao.countByPeriodo(dataInizio, dataFine);
    }
}
