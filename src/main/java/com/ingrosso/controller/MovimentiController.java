package com.ingrosso.controller;

import com.ingrosso.dao.MovimentoDao;
import com.ingrosso.model.*;
import com.ingrosso.service.MagazzinoService;
import com.ingrosso.service.MovimentoService;
import com.ingrosso.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MovimentiController {
    private static final Logger logger = LoggerFactory.getLogger(MovimentiController.class);

    @FXML private DatePicker dpDataInizio;
    @FXML private DatePicker dpDataFine;
    @FXML private ComboBox<Magazzino> cmbMagazzino;
    @FXML private ComboBox<TipoMovimento> cmbTipo;
    @FXML private TextField txtSearch;

    @FXML private TableView<MovimentoDao.MovimentoCompleto> tblMovimenti;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, String> colData;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, String> colTipo;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, String> colProdotto;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, BigDecimal> colQuantita;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, String> colMagazzino;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, String> colCausale;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, String> colDocumento;
    @FXML private TableColumn<MovimentoDao.MovimentoCompleto, String> colUtente;

    @FXML private Label lblTotaleMovimenti;

    private final MovimentoService movimentoService = MovimentoService.getInstance();
    private final MagazzinoService magazzinoService = MagazzinoService.getInstance();

    private ObservableList<MovimentoDao.MovimentoCompleto> allMovimenti = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        setupDatePickers();
        loadData();
    }

    private void setupComboBoxes() {
        // Magazzini
        List<Magazzino> magazzini = magazzinoService.getAllMagazzini();
        cmbMagazzino.getItems().add(null); // All option
        cmbMagazzino.getItems().addAll(magazzini);
        cmbMagazzino.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Magazzino m) {
                return m != null ? m.getNome() : "Tutti i magazzini";
            }

            @Override
            public Magazzino fromString(String s) {
                return null;
            }
        });

        // Tipi movimento
        cmbTipo.getItems().add(null); // All option
        cmbTipo.getItems().addAll(TipoMovimento.values());
        cmbTipo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(TipoMovimento t) {
                return t != null ? t.getDescrizione() : "Tutti i tipi";
            }

            @Override
            public TipoMovimento fromString(String s) {
                return null;
            }
        });
    }

    private void setupTable() {
        colData.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDataMovimento() != null) {
                return new SimpleStringProperty(FormatUtil.formatDateTime(cellData.getValue().getDataMovimento()));
            }
            return new SimpleStringProperty("");
        });

        colTipo.setCellValueFactory(cellData -> {
            TipoMovimento tipo = cellData.getValue().getTipo();
            return new SimpleStringProperty(tipo != null ? tipo.getDescrizione() : "");
        });

        colProdotto.setCellValueFactory(new PropertyValueFactory<>("prodottoNome"));
        colQuantita.setCellValueFactory(new PropertyValueFactory<>("quantita"));
        colMagazzino.setCellValueFactory(new PropertyValueFactory<>("magazzinoNome"));
        colCausale.setCellValueFactory(new PropertyValueFactory<>("causale"));
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documentoRif"));
        colUtente.setCellValueFactory(new PropertyValueFactory<>("utenteNome"));

        // Color code by movement type
        tblMovimenti.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(MovimentoDao.MovimentoCompleto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    switch (item.getTipo()) {
                        case CARICO -> setStyle("-fx-background-color: #DCFCE7;");
                        case SCARICO -> setStyle("-fx-background-color: #FEE2E2;");
                        case RETTIFICA -> setStyle("-fx-background-color: #FEF9C3;");
                        case TRASFERIMENTO -> setStyle("-fx-background-color: #DBEAFE;");
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    private void setupDatePickers() {
        // Default to last 30 days
        LocalDate today = LocalDate.now();
        dpDataFine.setValue(today);
        dpDataInizio.setValue(today.minusDays(30));
    }

    private void loadData() {
        LocalDate dataInizio = dpDataInizio.getValue();
        LocalDate dataFine = dpDataFine.getValue();
        Magazzino magazzino = cmbMagazzino.getValue();
        TipoMovimento tipo = cmbTipo.getValue();

        Integer magazzinoId = magazzino != null ? magazzino.getId() : null;

        List<MovimentoDao.MovimentoCompleto> movimenti = movimentoService.getMovimentiCompleti(
                dataInizio, dataFine, magazzinoId, tipo);

        allMovimenti.setAll(movimenti);
        tblMovimenti.setItems(allMovimenti);

        updateTotals();
    }

    @FXML
    public void handleFilter() {
        loadData();
    }

    @FXML
    public void handleSearch() {
        String searchText = txtSearch.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            tblMovimenti.setItems(allMovimenti);
        } else {
            ObservableList<MovimentoDao.MovimentoCompleto> filtered = allMovimenti.filtered(m ->
                    (m.getProdottoNome() != null && m.getProdottoNome().toLowerCase().contains(searchText)) ||
                            (m.getCausale() != null && m.getCausale().toLowerCase().contains(searchText)) ||
                            (m.getDocumentoRif() != null && m.getDocumentoRif().toLowerCase().contains(searchText)));
            tblMovimenti.setItems(filtered);
        }

        updateTotals();
    }

    private void updateTotals() {
        lblTotaleMovimenti.setText(String.valueOf(tblMovimenti.getItems().size()));
    }

    @FXML
    public void nuovoMovimento() {
        showMovimentoDialog(null);
    }

    private void showMovimentoDialog(Movimento movimento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/movimento-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            MovimentoDialogController controller = loader.getController();
            controller.setMovimento(movimento);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(movimento == null ? "Nuovo Movimento" : "Modifica Movimento");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Movimento newMovimento = controller.getMovimento();
                if (newMovimento != null) {
                    movimentoService.registraMovimento(newMovimento);
                    loadData();
                }
            }
        } catch (IOException e) {
            logger.error("Error opening movimento dialog: {}", e.getMessage());
        }
    }

    @FXML
    public void refreshData() {
        loadData();
    }
}
