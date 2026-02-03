package com.ingrosso.service;

import com.ingrosso.dao.DdtDao;
import com.ingrosso.dao.ProdottoDao;
import com.ingrosso.dao.UtenteDao;
import com.ingrosso.model.*;
import com.ingrosso.util.PdfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DdtService {
    private static final Logger logger = LoggerFactory.getLogger(DdtService.class);
    private static DdtService instance;

    private final DdtDao ddtDao;
    private final ProdottoDao prodottoDao;
    private final UtenteDao utenteDao;
    private final MovimentoService movimentoService;

    private DdtService() {
        this.ddtDao = new DdtDao();
        this.prodottoDao = new ProdottoDao();
        this.utenteDao = new UtenteDao();
        this.movimentoService = MovimentoService.getInstance();
    }

    public static synchronized DdtService getInstance() {
        if (instance == null) {
            instance = new DdtService();
        }
        return instance;
    }

    // DDT
    public List<Ddt> getDdtByAnno(int anno) {
        List<Ddt> ddtList = ddtDao.findByAnno(anno);
        ddtList.forEach(this::enrichDdt);
        return ddtList;
    }

    public List<Ddt> getDdtByPeriodo(LocalDate dataInizio, LocalDate dataFine) {
        List<Ddt> ddtList = ddtDao.findByPeriodo(dataInizio, dataFine);
        ddtList.forEach(this::enrichDdt);
        return ddtList;
    }

    public List<Ddt> getDdtByStato(StatoDdt stato) {
        List<Ddt> ddtList = ddtDao.findByStato(stato);
        ddtList.forEach(this::enrichDdt);
        return ddtList;
    }

    public Optional<Ddt> getDdtById(int id) {
        Optional<Ddt> ddtOpt = ddtDao.findById(id);
        ddtOpt.ifPresent(ddt -> {
            enrichDdt(ddt);
            // Load righe
            List<DdtRiga> righe = ddtDao.findRigheByDdt(id);
            righe.forEach(this::enrichRiga);
            ddt.getRighe().setAll(righe);
        });
        return ddtOpt;
    }

    public Ddt createNewDdt(int magazzinoId, Integer utenteId) {
        Ddt ddt = new Ddt();
        int anno = LocalDate.now().getYear();
        ddt.setAnno(anno);
        ddt.setNumero(ddtDao.getNextNumero(anno));
        ddt.setMagazzinoId(magazzinoId);
        if (utenteId != null) ddt.setUtenteId(utenteId);
        return ddt;
    }

    public int saveDdt(Ddt ddt) {
        int ddtId;
        if (ddt.getId() > 0) {
            ddtDao.update(ddt);
            ddtId = ddt.getId();
        } else {
            ddtId = ddtDao.insert(ddt);
        }

        if (ddtId > 0) {
            // Save righe
            ddtDao.deleteRigheByDdt(ddtId);
            int ordine = 1;
            for (DdtRiga riga : ddt.getRighe()) {
                riga.setDdtId(ddtId);
                riga.setOrdine(ordine++);
                ddtDao.insertRiga(riga);
            }
        }

        return ddtId;
    }

    public boolean emitDdt(int ddtId) {
        Optional<Ddt> ddtOpt = getDdtById(ddtId);
        if (ddtOpt.isEmpty()) {
            logger.error("DDT not found: {}", ddtId);
            return false;
        }

        Ddt ddt = ddtOpt.get();
        if (ddt.getStato() != StatoDdt.BOZZA) {
            logger.error("DDT not in BOZZA state: {}", ddtId);
            return false;
        }

        // Scarica quantita dal magazzino
        String documentoRif = "DDT " + ddt.getNumeroCompleto();
        for (DdtRiga riga : ddt.getRighe()) {
            int result = movimentoService.registraScarico(
                    riga.getProdottoId(),
                    ddt.getMagazzinoId(),
                    riga.getQuantita(),
                    "Emissione DDT",
                    documentoRif,
                    riga.getLottoId() > 0 ? riga.getLottoId() : null,
                    ddt.getUtenteId() > 0 ? ddt.getUtenteId() : null
            );
            if (result < 0) {
                logger.error("Failed to register scarico for DDT riga: {}", riga.getId());
                return false;
            }
        }

        // Update stato
        return ddtDao.updateStato(ddtId, StatoDdt.EMESSO);
    }

    public boolean annullaDdt(int ddtId) {
        Optional<Ddt> ddtOpt = getDdtById(ddtId);
        if (ddtOpt.isEmpty()) {
            return false;
        }

        Ddt ddt = ddtOpt.get();

        // If was EMESSO, restore quantities
        if (ddt.getStato() == StatoDdt.EMESSO) {
            String documentoRif = "Annullamento DDT " + ddt.getNumeroCompleto();
            for (DdtRiga riga : ddt.getRighe()) {
                movimentoService.registraCarico(
                        riga.getProdottoId(),
                        ddt.getMagazzinoId(),
                        riga.getQuantita(),
                        "Annullamento DDT",
                        documentoRif,
                        riga.getLottoId() > 0 ? riga.getLottoId() : null,
                        ddt.getUtenteId() > 0 ? ddt.getUtenteId() : null
                );
            }
        }

        return ddtDao.updateStato(ddtId, StatoDdt.ANNULLATO);
    }

    public boolean deleteDdt(int ddtId) {
        Optional<Ddt> ddtOpt = ddtDao.findById(ddtId);
        if (ddtOpt.isEmpty()) return false;

        if (ddtOpt.get().getStato() != StatoDdt.BOZZA) {
            logger.warn("Cannot delete DDT not in BOZZA state");
            return false;
        }

        return ddtDao.deleteById(ddtId);
    }

    public byte[] generatePdf(int ddtId) {
        Optional<Ddt> ddtOpt = getDdtById(ddtId);
        if (ddtOpt.isEmpty()) return null;

        ConfigAzienda config = utenteDao.getConfigAzienda().orElse(null);
        return PdfUtil.generateDdtPdf(ddtOpt.get(), config);
    }

    private void enrichDdt(Ddt ddt) {
        if (ddt.getDestinatarioId() > 0) {
            ddtDao.findDestinatarioById(ddt.getDestinatarioId())
                    .ifPresent(ddt::setDestinatario);
        }
    }

    private void enrichRiga(DdtRiga riga) {
        if (riga.getProdottoId() > 0) {
            prodottoDao.findById(riga.getProdottoId())
                    .ifPresent(riga::setProdotto);
        }
    }

    // Destinatari
    public List<Destinatario> getAllDestinatari() {
        return ddtDao.findAllDestinatari();
    }

    public Optional<Destinatario> getDestinatarioById(int id) {
        return ddtDao.findDestinatarioById(id);
    }

    public List<Destinatario> searchDestinatari(String text) {
        return ddtDao.searchDestinatari(text);
    }

    public int saveDestinatario(Destinatario dest) {
        if (dest.getId() > 0) {
            return ddtDao.updateDestinatario(dest) ? dest.getId() : -1;
        } else {
            return ddtDao.insertDestinatario(dest);
        }
    }

    // Stats
    public int countDdtMeseCorrente() {
        LocalDate now = LocalDate.now();
        return ddtDao.countByMeseAnno(now.getMonthValue(), now.getYear());
    }
}
