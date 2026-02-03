package com.ingrosso.controller;

import com.ingrosso.dao.GiacenzaDao;
import com.ingrosso.dao.LottoDao;
import com.ingrosso.service.MagazzinoService;
import com.ingrosso.service.ReportService;
import com.ingrosso.util.FormatUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    // KPI Labels
    @FXML private Label lblTotaleProdotti;
    @FXML private Label lblValoreMagazzino;
    @FXML private Label lblSottoScorta;
    @FXML private Label lblLottiScadenza;
    @FXML private Label lblDdtMese;

    // Alerts tables
    @FXML private TableView<GiacenzaDao.GiacenzaCompleta> tblSottoScorta;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, String> colSottoScortaCodice;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, String> colSottoScortaNome;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, BigDecimal> colSottoScortaGiacenza;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, BigDecimal> colSottoScortaMinima;

    @FXML private TableView<LottoDao.LottoCompleto> tblLottiScadenza;
    @FXML private TableColumn<LottoDao.LottoCompleto, String> colLottoProdotto;
    @FXML private TableColumn<LottoDao.LottoCompleto, String> colLottoNumero;
    @FXML private TableColumn<LottoDao.LottoCompleto, String> colLottoScadenza;
    @FXML private TableColumn<LottoDao.LottoCompleto, Integer> colLottoGiorni;

    private final ReportService reportService = ReportService.getInstance();
    private final MagazzinoService magazzinoService = MagazzinoService.getInstance();

    @FXML
    public void initialize() {
        setupTables();
        loadData();
    }

    private void setupTables() {
        // Sotto scorta table
        if (colSottoScortaCodice != null) {
            colSottoScortaCodice.setCellValueFactory(new PropertyValueFactory<>("codice"));
            colSottoScortaNome.setCellValueFactory(new PropertyValueFactory<>("prodotto"));
            colSottoScortaGiacenza.setCellValueFactory(new PropertyValueFactory<>("giacenza"));
            colSottoScortaMinima.setCellValueFactory(new PropertyValueFactory<>("scortaMinima"));
        }

        // Lotti scadenza table
        if (colLottoProdotto != null) {
            colLottoProdotto.setCellValueFactory(new PropertyValueFactory<>("prodotto"));
            colLottoNumero.setCellValueFactory(new PropertyValueFactory<>("numeroLotto"));
            colLottoScadenza.setCellValueFactory(cellData -> {
                if (cellData.getValue().getDataScadenza() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            FormatUtil.formatDate(cellData.getValue().getDataScadenza()));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
            colLottoGiorni.setCellValueFactory(new PropertyValueFactory<>("giorniAScadenza"));
        }
    }

    private void loadData() {
        try {
            ReportService.DashboardKpi kpi = reportService.getDashboardKpi();

            // Update KPI cards
            if (lblTotaleProdotti != null) {
                lblTotaleProdotti.setText(String.valueOf(kpi.totaleProdotti));
            }
            if (lblValoreMagazzino != null) {
                lblValoreMagazzino.setText(FormatUtil.formatCurrency(kpi.valoreMagazzinoVendita));
            }
            if (lblSottoScorta != null) {
                lblSottoScorta.setText(String.valueOf(kpi.prodottiSottoScorta));
                if (kpi.prodottiSottoScorta > 0) {
                    lblSottoScorta.getStyleClass().add("text-danger");
                }
            }
            if (lblLottiScadenza != null) {
                lblLottiScadenza.setText(String.valueOf(kpi.lottiInScadenza));
                if (kpi.lottiInScadenza > 0) {
                    lblLottiScadenza.getStyleClass().add("text-warning");
                }
            }
            if (lblDdtMese != null) {
                lblDdtMese.setText(String.valueOf(kpi.ddtMeseCorrente));
            }

            // Load alerts tables
            loadAlertsData();

        } catch (Exception e) {
            logger.error("Error loading dashboard data: {}", e.getMessage());
        }
    }

    private void loadAlertsData() {
        try {
            // Get magazzino principale
            magazzinoService.getMagazzinoPrincipale().ifPresent(mag -> {
                // Prodotti sotto scorta
                if (tblSottoScorta != null) {
                    List<GiacenzaDao.GiacenzaCompleta> sottoScorta =
                            reportService.getProdottiSottoScorta(mag.getId());
                    tblSottoScorta.getItems().setAll(sottoScorta);
                }
            });

            // Lotti in scadenza
            if (tblLottiScadenza != null) {
                List<LottoDao.LottoCompleto> lottiScadenza =
                        reportService.getLottiInScadenza(30);
                tblLottiScadenza.getItems().setAll(lottiScadenza);
            }

        } catch (Exception e) {
            logger.error("Error loading alerts data: {}", e.getMessage());
        }
    }

    @FXML
    public void refreshData() {
        loadData();
    }

    @FXML
    public void nuovoMovimento() {
        // Navigate to movimenti with new dialog
        logger.info("Opening new movimento dialog");
    }

    @FXML
    public void nuovoDdt() {
        // Navigate to DDT with new dialog
        logger.info("Opening new DDT dialog");
    }
}
