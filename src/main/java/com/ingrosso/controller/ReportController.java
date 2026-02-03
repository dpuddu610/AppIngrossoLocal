package com.ingrosso.controller;

import com.ingrosso.dao.GiacenzaDao;
import com.ingrosso.dao.LottoDao;
import com.ingrosso.dao.MovimentoDao;
import com.ingrosso.model.*;
import com.ingrosso.service.MagazzinoService;
import com.ingrosso.service.ProdottoService;
import com.ingrosso.service.ReportService;
import com.ingrosso.util.AlertUtil;
import com.ingrosso.util.FormatUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @FXML private ListView<String> lstReport;
    @FXML private Label lblReportTitolo;
    @FXML private VBox vboxFiltri;

    @FXML private DatePicker dpDataInizio;
    @FXML private DatePicker dpDataFine;
    @FXML private ComboBox<Magazzino> cmbMagazzino;
    @FXML private ComboBox<Categoria> cmbCategoria;

    @FXML private ScrollPane scrollPreview;
    @FXML private VBox vboxPreview;

    private final ReportService reportService = ReportService.getInstance();
    private final MagazzinoService magazzinoService = MagazzinoService.getInstance();
    private final ProdottoService prodottoService = ProdottoService.getInstance();

    private String selectedReport;
    private String lastGeneratedContent;

    private static final List<String> REPORT_LIST = List.of(
            "Situazione Magazzino",
            "Valore Magazzino",
            "Prodotti Sotto Scorta",
            "Lotti in Scadenza",
            "Movimenti Periodo",
            "Riepilogo DDT"
    );

    @FXML
    public void initialize() {
        setupReportList();
        setupFilters();
        setupDatePickers();
    }

    private void setupReportList() {
        lstReport.setItems(FXCollections.observableArrayList(REPORT_LIST));

        lstReport.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedReport = newVal;
            if (newVal != null) {
                lblReportTitolo.setText(newVal);
                updateFiltersVisibility();
            }
        });
    }

    private void setupFilters() {
        // Magazzini
        List<Magazzino> magazzini = magazzinoService.getAllMagazzini();
        cmbMagazzino.getItems().add(null);
        cmbMagazzino.getItems().addAll(magazzini);
        cmbMagazzino.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Magazzino m) {
                return m != null ? m.getNome() : "Tutti";
            }

            @Override
            public Magazzino fromString(String s) {
                return null;
            }
        });
        magazzinoService.getMagazzinoPrincipale().ifPresent(cmbMagazzino::setValue);

        // Categorie
        List<Categoria> categorie = prodottoService.getAllCategorie();
        cmbCategoria.getItems().add(null);
        cmbCategoria.getItems().addAll(categorie);
        cmbCategoria.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categoria c) {
                return c != null ? c.getNome() : "Tutte";
            }

            @Override
            public Categoria fromString(String s) {
                return null;
            }
        });
    }

    private void setupDatePickers() {
        LocalDate today = LocalDate.now();
        dpDataFine.setValue(today);
        dpDataInizio.setValue(today.withDayOfMonth(1));
    }

    private void updateFiltersVisibility() {
        // Show/hide filters based on selected report
        boolean needsDates = selectedReport != null && (
                selectedReport.equals("Movimenti Periodo") ||
                        selectedReport.equals("Riepilogo DDT"));

        dpDataInizio.setVisible(needsDates);
        dpDataFine.setVisible(needsDates);
    }

    @FXML
    public void generaReport() {
        if (selectedReport == null) {
            AlertUtil.showWarning("Attenzione", "Selezionare un report");
            return;
        }

        vboxPreview.getChildren().clear();

        switch (selectedReport) {
            case "Situazione Magazzino" -> generaSituazioneMagazzino();
            case "Valore Magazzino" -> generaValoreMagazzino();
            case "Prodotti Sotto Scorta" -> generaProdottiSottoScorta();
            case "Lotti in Scadenza" -> generaLottiScadenza();
            case "Movimenti Periodo" -> generaMovimentiPeriodo();
            case "Riepilogo DDT" -> generaRiepilogoDdt();
        }
    }

    private void generaSituazioneMagazzino() {
        Magazzino mag = cmbMagazzino.getValue();
        if (mag == null) {
            AlertUtil.showWarning("Attenzione", "Selezionare un magazzino");
            return;
        }

        List<GiacenzaDao.GiacenzaCompleta> giacenze = reportService.getSituazioneMagazzino(mag.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("SITUAZIONE MAGAZZINO: ").append(mag.getNome()).append("\n");
        sb.append("Data: ").append(FormatUtil.formatDate(LocalDate.now())).append("\n\n");

        sb.append(String.format("%-15s %-30s %-15s %-10s %-15s %-15s\n",
                "Codice", "Prodotto", "Giacenza", "U.M.", "Val. Acq.", "Val. Vend."));
        sb.append("-".repeat(100)).append("\n");

        BigDecimal totaleAcquisto = BigDecimal.ZERO;
        BigDecimal totaleVendita = BigDecimal.ZERO;

        for (GiacenzaDao.GiacenzaCompleta g : giacenze) {
            sb.append(String.format("%-15s %-30s %15s %-10s %15s %15s\n",
                    truncate(g.getCodice(), 15),
                    truncate(g.getProdotto(), 30),
                    FormatUtil.formatQuantity(g.getGiacenza()),
                    g.getUnitaMisura(),
                    FormatUtil.formatCurrencyNoSymbol(g.getValoreAcquisto()),
                    FormatUtil.formatCurrencyNoSymbol(g.getValoreVendita())));

            totaleAcquisto = totaleAcquisto.add(g.getValoreAcquisto());
            totaleVendita = totaleVendita.add(g.getValoreVendita());
        }

        sb.append("-".repeat(100)).append("\n");
        sb.append(String.format("%-62s %15s %15s\n",
                "TOTALE:",
                FormatUtil.formatCurrencyNoSymbol(totaleAcquisto),
                FormatUtil.formatCurrencyNoSymbol(totaleVendita)));

        lastGeneratedContent = sb.toString();
        showPreview(sb.toString());
    }

    private void generaValoreMagazzino() {
        Magazzino mag = cmbMagazzino.getValue();

        StringBuilder sb = new StringBuilder();
        sb.append("VALORE MAGAZZINO PER CATEGORIA\n");
        sb.append("Data: ").append(FormatUtil.formatDate(LocalDate.now())).append("\n\n");

        if (mag != null) {
            sb.append("Magazzino: ").append(mag.getNome()).append("\n\n");
            Map<String, BigDecimal> valori = reportService.getValorePerCategoria(mag.getId());

            BigDecimal totale = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : valori.entrySet()) {
                sb.append(String.format("%-30s %15s\n",
                        entry.getKey(),
                        FormatUtil.formatCurrency(entry.getValue())));
                totale = totale.add(entry.getValue());
            }

            sb.append("-".repeat(45)).append("\n");
            sb.append(String.format("%-30s %15s\n", "TOTALE:", FormatUtil.formatCurrency(totale)));
        }

        lastGeneratedContent = sb.toString();
        showPreview(sb.toString());
    }

    private void generaProdottiSottoScorta() {
        Magazzino mag = cmbMagazzino.getValue();
        if (mag == null) {
            AlertUtil.showWarning("Attenzione", "Selezionare un magazzino");
            return;
        }

        List<GiacenzaDao.GiacenzaCompleta> sottoScorta = reportService.getProdottiSottoScorta(mag.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("PRODOTTI SOTTO SCORTA\n");
        sb.append("Magazzino: ").append(mag.getNome()).append("\n");
        sb.append("Data: ").append(FormatUtil.formatDate(LocalDate.now())).append("\n\n");

        if (sottoScorta.isEmpty()) {
            sb.append("Nessun prodotto sotto scorta minima.\n");
        } else {
            sb.append(String.format("%-15s %-35s %-12s %-12s %-12s\n",
                    "Codice", "Prodotto", "Giacenza", "Scorta Min", "Mancante"));
            sb.append("-".repeat(90)).append("\n");

            for (GiacenzaDao.GiacenzaCompleta g : sottoScorta) {
                BigDecimal mancante = g.getScortaMinima().subtract(g.getGiacenza());
                sb.append(String.format("%-15s %-35s %12s %12s %12s\n",
                        truncate(g.getCodice(), 15),
                        truncate(g.getProdotto(), 35),
                        FormatUtil.formatQuantity(g.getGiacenza()),
                        FormatUtil.formatQuantity(g.getScortaMinima()),
                        FormatUtil.formatQuantity(mancante)));
            }

            sb.append("-".repeat(90)).append("\n");
            sb.append("Totale prodotti sotto scorta: ").append(sottoScorta.size());
        }

        lastGeneratedContent = sb.toString();
        showPreview(sb.toString());
    }

    private void generaLottiScadenza() {
        List<LottoDao.LottoCompleto> lotti = reportService.getLottiInScadenza(30);

        StringBuilder sb = new StringBuilder();
        sb.append("LOTTI IN SCADENZA (prossimi 30 giorni)\n");
        sb.append("Data: ").append(FormatUtil.formatDate(LocalDate.now())).append("\n\n");

        if (lotti.isEmpty()) {
            sb.append("Nessun lotto in scadenza.\n");
        } else {
            sb.append(String.format("%-30s %-15s %-12s %-12s %-10s\n",
                    "Prodotto", "Lotto", "Scadenza", "Quantita'", "Giorni"));
            sb.append("-".repeat(80)).append("\n");

            for (LottoDao.LottoCompleto l : lotti) {
                sb.append(String.format("%-30s %-15s %-12s %12s %10d\n",
                        truncate(l.getProdotto(), 30),
                        truncate(l.getNumeroLotto(), 15),
                        FormatUtil.formatDate(l.getDataScadenza()),
                        FormatUtil.formatQuantity(l.getQuantita()),
                        l.getGiorniAScadenza()));
            }
        }

        lastGeneratedContent = sb.toString();
        showPreview(sb.toString());
    }

    private void generaMovimentiPeriodo() {
        LocalDate dataInizio = dpDataInizio.getValue();
        LocalDate dataFine = dpDataFine.getValue();

        if (dataInizio == null || dataFine == null) {
            AlertUtil.showWarning("Attenzione", "Selezionare il periodo");
            return;
        }

        Integer magazzinoId = cmbMagazzino.getValue() != null ? cmbMagazzino.getValue().getId() : null;
        List<MovimentoDao.MovimentoCompleto> movimenti =
                reportService.getMovimentiPeriodo(dataInizio, dataFine, magazzinoId, null);

        StringBuilder sb = new StringBuilder();
        sb.append("MOVIMENTI PERIODO\n");
        sb.append("Dal ").append(FormatUtil.formatDate(dataInizio));
        sb.append(" al ").append(FormatUtil.formatDate(dataFine)).append("\n\n");

        sb.append(String.format("%-12s %-12s %-25s %12s %-20s\n",
                "Data", "Tipo", "Prodotto", "Quantita'", "Causale"));
        sb.append("-".repeat(85)).append("\n");

        for (MovimentoDao.MovimentoCompleto m : movimenti) {
            sb.append(String.format("%-12s %-12s %-25s %12s %-20s\n",
                    FormatUtil.formatDate(m.getDataMovimento().toLocalDate()),
                    m.getTipo().getDescrizione(),
                    truncate(m.getProdottoNome(), 25),
                    FormatUtil.formatQuantity(m.getQuantita()),
                    truncate(m.getCausale() != null ? m.getCausale() : "", 20)));
        }

        sb.append("-".repeat(85)).append("\n");
        sb.append("Totale movimenti: ").append(movimenti.size());

        lastGeneratedContent = sb.toString();
        showPreview(sb.toString());
    }

    private void generaRiepilogoDdt() {
        LocalDate dataInizio = dpDataInizio.getValue();
        LocalDate dataFine = dpDataFine.getValue();

        if (dataInizio == null || dataFine == null) {
            AlertUtil.showWarning("Attenzione", "Selezionare il periodo");
            return;
        }

        ReportService.RiepilogoDdt riepilogo = reportService.getRiepilogoDdt(dataInizio, dataFine);

        StringBuilder sb = new StringBuilder();
        sb.append("RIEPILOGO DDT\n");
        sb.append("Dal ").append(FormatUtil.formatDate(dataInizio));
        sb.append(" al ").append(FormatUtil.formatDate(dataFine)).append("\n\n");

        sb.append("Totale DDT:      ").append(riepilogo.totaleDdt).append("\n");
        sb.append("  - Emessi:      ").append(riepilogo.ddtEmessi).append("\n");
        sb.append("  - Bozza:       ").append(riepilogo.ddtBozza).append("\n");
        sb.append("  - Annullati:   ").append(riepilogo.ddtAnnullati).append("\n\n");
        sb.append("Totale righe:    ").append(riepilogo.totaleRighe).append("\n");
        sb.append("Totale importo:  ").append(FormatUtil.formatCurrency(riepilogo.totaleImporto)).append("\n");

        lastGeneratedContent = sb.toString();
        showPreview(sb.toString());
    }

    private void showPreview(String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        textArea.setPrefRowCount(30);

        vboxPreview.getChildren().add(textArea);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    @FXML
    public void esportaPdf() {
        AlertUtil.showInfo("Export PDF", "Funzionalita' in sviluppo");
    }

    @FXML
    public void esportaExcel() {
        if (lastGeneratedContent == null) {
            AlertUtil.showWarning("Attenzione", "Generare prima il report");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Esporta Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("report.txt");

        File file = fileChooser.showSaveDialog(lstReport.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(lastGeneratedContent);
                AlertUtil.showInfo("Export completato", "File salvato in " + file.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Error exporting report: {}", e.getMessage());
                AlertUtil.showError("Errore", "Impossibile esportare il report");
            }
        }
    }
}
