package com.ingrosso.service;

import com.ingrosso.dao.*;
import com.ingrosso.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private static ReportService instance;

    private final ProdottoDao prodottoDao;
    private final GiacenzaDao giacenzaDao;
    private final MovimentoDao movimentoDao;
    private final LottoDao lottoDao;
    private final DdtDao ddtDao;
    private final CategoriaDao categoriaDao;
    private final MagazzinoDao magazzinoDao;

    private ReportService() {
        this.prodottoDao = new ProdottoDao();
        this.giacenzaDao = new GiacenzaDao();
        this.movimentoDao = new MovimentoDao();
        this.lottoDao = new LottoDao();
        this.ddtDao = new DdtDao();
        this.categoriaDao = new CategoriaDao();
        this.magazzinoDao = new MagazzinoDao();
    }

    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    // Dashboard KPIs
    public DashboardKpi getDashboardKpi() {
        DashboardKpi kpi = new DashboardKpi();

        // Prodotti attivi
        kpi.totaleProdotti = prodottoDao.findAllActive().size();

        // Magazzino principale
        magazzinoDao.findPrincipale().ifPresent(mag -> {
            List<GiacenzaDao.GiacenzaCompleta> giacenze = giacenzaDao.findGiacenzeComplete(mag.getId());

            // Valore magazzino
            kpi.valoreMagazzinoAcquisto = giacenze.stream()
                    .map(GiacenzaDao.GiacenzaCompleta::getValoreAcquisto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            kpi.valoreMagazzinoVendita = giacenze.stream()
                    .map(GiacenzaDao.GiacenzaCompleta::getValoreVendita)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sotto scorta
            kpi.prodottiSottoScorta = (int) giacenze.stream()
                    .filter(GiacenzaDao.GiacenzaCompleta::isSottoScorta)
                    .count();
        });

        // Lotti in scadenza
        kpi.lottiInScadenza = lottoDao.findLottiInScadenzaCompleti(30).size();

        // DDT mese corrente
        LocalDate now = LocalDate.now();
        kpi.ddtMeseCorrente = ddtDao.countByMeseAnno(now.getMonthValue(), now.getYear());

        // Movimenti ultimi 30 giorni
        kpi.movimentiUltimi30gg = movimentoDao.countByPeriodo(
                now.minusDays(30), now);

        return kpi;
    }

    // Valore magazzino per categoria
    public Map<String, BigDecimal> getValorePerCategoria(int magazzinoId) {
        Map<String, BigDecimal> result = new HashMap<>();

        List<GiacenzaDao.GiacenzaCompleta> giacenze = giacenzaDao.findGiacenzeComplete(magazzinoId);
        for (GiacenzaDao.GiacenzaCompleta g : giacenze) {
            String categoria = g.getCategoria() != null ? g.getCategoria() : "Senza categoria";
            result.merge(categoria, g.getValoreVendita(), BigDecimal::add);
        }

        return result;
    }

    // Prodotti sotto scorta
    public List<GiacenzaDao.GiacenzaCompleta> getProdottiSottoScorta(int magazzinoId) {
        return giacenzaDao.findGiacenzeComplete(magazzinoId).stream()
                .filter(GiacenzaDao.GiacenzaCompleta::isSottoScorta)
                .toList();
    }

    // Lotti in scadenza
    public List<LottoDao.LottoCompleto> getLottiInScadenza(int giorni) {
        return lottoDao.findLottiInScadenzaCompleti(giorni);
    }

    // Movimenti periodo
    public List<MovimentoDao.MovimentoCompleto> getMovimentiPeriodo(LocalDate dataInizio, LocalDate dataFine,
                                                                     Integer magazzinoId, TipoMovimento tipo) {
        return movimentoDao.findMovimentiCompleti(dataInizio, dataFine, magazzinoId, tipo);
    }

    // Situazione magazzino completa
    public List<GiacenzaDao.GiacenzaCompleta> getSituazioneMagazzino(int magazzinoId) {
        return giacenzaDao.findGiacenzeComplete(magazzinoId);
    }

    // Riepilogo DDT per periodo
    public RiepilogoDdt getRiepilogoDdt(LocalDate dataInizio, LocalDate dataFine) {
        RiepilogoDdt riepilogo = new RiepilogoDdt();

        List<Ddt> ddtList = ddtDao.findByPeriodo(dataInizio, dataFine);

        riepilogo.totaleDdt = ddtList.size();
        riepilogo.ddtEmessi = (int) ddtList.stream()
                .filter(d -> d.getStato() == StatoDdt.EMESSO)
                .count();
        riepilogo.ddtBozza = (int) ddtList.stream()
                .filter(d -> d.getStato() == StatoDdt.BOZZA)
                .count();
        riepilogo.ddtAnnullati = (int) ddtList.stream()
                .filter(d -> d.getStato() == StatoDdt.ANNULLATO)
                .count();

        // Totale righe e importo (only for emessi)
        for (Ddt ddt : ddtList) {
            if (ddt.getStato() == StatoDdt.EMESSO) {
                List<DdtRiga> righe = ddtDao.findRigheByDdt(ddt.getId());
                riepilogo.totaleRighe += righe.size();
                for (DdtRiga riga : righe) {
                    if (riga.getQuantita() != null && riga.getPrezzoUnitario() != null) {
                        riepilogo.totaleImporto = riepilogo.totaleImporto.add(
                                riga.getQuantita().multiply(riga.getPrezzoUnitario()));
                    }
                }
            }
        }

        return riepilogo;
    }

    // Top prodotti per valore
    public List<GiacenzaDao.GiacenzaCompleta> getTopProdottiPerValore(int magazzinoId, int limit) {
        return giacenzaDao.findGiacenzeComplete(magazzinoId).stream()
                .sorted((a, b) -> b.getValoreVendita().compareTo(a.getValoreVendita()))
                .limit(limit)
                .toList();
    }

    public static class DashboardKpi {
        public int totaleProdotti;
        public BigDecimal valoreMagazzinoAcquisto = BigDecimal.ZERO;
        public BigDecimal valoreMagazzinoVendita = BigDecimal.ZERO;
        public int prodottiSottoScorta;
        public int lottiInScadenza;
        public int ddtMeseCorrente;
        public int movimentiUltimi30gg;
    }

    public static class RiepilogoDdt {
        public int totaleDdt;
        public int ddtEmessi;
        public int ddtBozza;
        public int ddtAnnullati;
        public int totaleRighe;
        public BigDecimal totaleImporto = BigDecimal.ZERO;
    }
}
