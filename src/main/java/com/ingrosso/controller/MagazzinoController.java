package com.ingrosso.controller;

import com.ingrosso.dao.GiacenzaDao;
import com.ingrosso.model.Magazzino;
import com.ingrosso.service.MagazzinoService;
import com.ingrosso.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class MagazzinoController {
    private static final Logger logger = LoggerFactory.getLogger(MagazzinoController.class);

    @FXML private ComboBox<Magazzino> cmbMagazzino;
    @FXML private TextField txtSearch;
    @FXML private CheckBox chkSottoScorta;

    @FXML private TableView<GiacenzaDao.GiacenzaCompleta> tblGiacenze;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, String> colCodice;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, String> colNome;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, String> colCategoria;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, BigDecimal> colGiacenza;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, String> colUM;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, BigDecimal> colScortaMin;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, BigDecimal> colValoreAcquisto;
    @FXML private TableColumn<GiacenzaDao.GiacenzaCompleta, BigDecimal> colValoreVendita;

    @FXML private Label lblTotaleArticoli;
    @FXML private Label lblValoreAcquisto;
    @FXML private Label lblValoreVendita;

    private final MagazzinoService magazzinoService = MagazzinoService.getInstance();

    private ObservableList<GiacenzaDao.GiacenzaCompleta> allGiacenze = FXCollections.observableArrayList();
    private FilteredList<GiacenzaDao.GiacenzaCompleta> filteredGiacenze;

    @FXML
    public void initialize() {
        setupComboBox();
        setupTable();
        loadMagazzini();
    }

    private void setupComboBox() {
        cmbMagazzino.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Magazzino m) {
                return m != null ? m.getNome() : "";
            }

            @Override
            public Magazzino fromString(String s) {
                return null;
            }
        });

        cmbMagazzino.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadGiacenze(newVal.getId());
            }
        });
    }

    private void setupTable() {
        colCodice.setCellValueFactory(new PropertyValueFactory<>("codice"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("prodotto"));
        colCategoria.setCellValueFactory(cellData -> {
            GiacenzaDao.GiacenzaCompleta g = cellData.getValue();
            String cat = g.getCategoria() != null ? g.getCategoria() : "";
            String subcat = g.getSottocategoria() != null ? " / " + g.getSottocategoria() : "";
            return new SimpleStringProperty(cat + subcat);
        });
        colGiacenza.setCellValueFactory(new PropertyValueFactory<>("giacenza"));
        colUM.setCellValueFactory(new PropertyValueFactory<>("unitaMisura"));
        colScortaMin.setCellValueFactory(new PropertyValueFactory<>("scortaMinima"));

        colValoreAcquisto.setCellValueFactory(new PropertyValueFactory<>("valoreAcquisto"));
        colValoreAcquisto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : FormatUtil.formatCurrencyNoSymbol(item));
            }
        });

        colValoreVendita.setCellValueFactory(new PropertyValueFactory<>("valoreVendita"));
        colValoreVendita.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : FormatUtil.formatCurrencyNoSymbol(item));
            }
        });

        // Highlight rows under minimum stock
        tblGiacenze.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(GiacenzaDao.GiacenzaCompleta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isSottoScorta()) {
                    setStyle("-fx-background-color: #FEE2E2;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadMagazzini() {
        List<Magazzino> magazzini = magazzinoService.getAllMagazzini();
        cmbMagazzino.getItems().setAll(magazzini);

        // Select principale or first
        magazzinoService.getMagazzinoPrincipale().ifPresentOrElse(
                mag -> cmbMagazzino.setValue(mag),
                () -> {
                    if (!magazzini.isEmpty()) {
                        cmbMagazzino.setValue(magazzini.get(0));
                    }
                }
        );
    }

    private void loadGiacenze(int magazzinoId) {
        List<GiacenzaDao.GiacenzaCompleta> giacenze = magazzinoService.getGiacenzeComplete(magazzinoId);
        allGiacenze.setAll(giacenze);
        filteredGiacenze = new FilteredList<>(allGiacenze, g -> true);
        tblGiacenze.setItems(filteredGiacenze);

        applyFilters();
        updateTotals();
    }

    @FXML
    public void handleSearch() {
        applyFilters();
    }

    @FXML
    public void handleFilter() {
        applyFilters();
    }

    private void applyFilters() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        boolean onlySottoScorta = chkSottoScorta.isSelected();

        filteredGiacenze.setPredicate(g -> {
            if (!searchText.isEmpty()) {
                boolean matches = g.getCodice().toLowerCase().contains(searchText) ||
                        g.getProdotto().toLowerCase().contains(searchText);
                if (!matches) return false;
            }

            if (onlySottoScorta && !g.isSottoScorta()) {
                return false;
            }

            return true;
        });

        updateTotals();
    }

    private void updateTotals() {
        int totaleArticoli = filteredGiacenze.size();
        BigDecimal totaleAcquisto = filteredGiacenze.stream()
                .map(GiacenzaDao.GiacenzaCompleta::getValoreAcquisto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totaleVendita = filteredGiacenze.stream()
                .map(GiacenzaDao.GiacenzaCompleta::getValoreVendita)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotaleArticoli.setText(String.valueOf(totaleArticoli));
        lblValoreAcquisto.setText(FormatUtil.formatCurrency(totaleAcquisto));
        lblValoreVendita.setText(FormatUtil.formatCurrency(totaleVendita));
    }

    @FXML
    public void refreshData() {
        Magazzino selected = cmbMagazzino.getValue();
        if (selected != null) {
            loadGiacenze(selected.getId());
        }
    }

    @FXML
    public void nuovoMovimento() {
        GiacenzaDao.GiacenzaCompleta selected = tblGiacenze.getSelectionModel().getSelectedItem();
        // Open movimento dialog with selected product
        logger.info("Opening movimento dialog for product: {}",
                selected != null ? selected.getCodice() : "none");
    }
}
