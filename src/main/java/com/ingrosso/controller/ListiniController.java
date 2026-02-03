package com.ingrosso.controller;

import com.ingrosso.model.*;
import com.ingrosso.service.ListinoService;
import com.ingrosso.util.AlertUtil;
import com.ingrosso.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ListiniController {
    private static final Logger logger = LoggerFactory.getLogger(ListiniController.class);

    @FXML private TableView<Listino> tblListini;
    @FXML private TableColumn<Listino, String> colCodice;
    @FXML private TableColumn<Listino, String> colNome;
    @FXML private TableColumn<Listino, String> colTipo;
    @FXML private TableColumn<Listino, String> colValidita;
    @FXML private TableColumn<Listino, Boolean> colAttivo;

    @FXML private Label lblListinoSelezionato;
    @FXML private Button btnModificaListino;
    @FXML private TextField txtSearchPrezzi;

    @FXML private TableView<ListinoPrezzo> tblPrezzi;
    @FXML private TableColumn<ListinoPrezzo, String> colProdottoCodice;
    @FXML private TableColumn<ListinoPrezzo, String> colProdottoNome;
    @FXML private TableColumn<ListinoPrezzo, BigDecimal> colPrezzo;
    @FXML private TableColumn<ListinoPrezzo, String> colDataInizio;
    @FXML private TableColumn<ListinoPrezzo, String> colDataFine;

    private final ListinoService listinoService = ListinoService.getInstance();

    private ObservableList<Listino> allListini = FXCollections.observableArrayList();
    private ObservableList<ListinoPrezzo> allPrezzi = FXCollections.observableArrayList();
    private Listino selectedListino;

    @FXML
    public void initialize() {
        setupListiniTable();
        setupPrezziTable();
        loadListini();
    }

    private void setupListiniTable() {
        colCodice.setCellValueFactory(new PropertyValueFactory<>("codice"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        colTipo.setCellValueFactory(cellData -> {
            TipoListino tipo = cellData.getValue().getTipo();
            return new SimpleStringProperty(tipo != null ? tipo.getDescrizione() : "");
        });

        colValidita.setCellValueFactory(cellData -> {
            Listino l = cellData.getValue();
            String validita = "";
            if (l.getDataValiditaInizio() != null) {
                validita = FormatUtil.formatDate(l.getDataValiditaInizio());
                if (l.getDataValiditaFine() != null) {
                    validita += " - " + FormatUtil.formatDate(l.getDataValiditaFine());
                }
            }
            return new SimpleStringProperty(validita);
        });

        colAttivo.setCellValueFactory(new PropertyValueFactory<>("attivo"));
        colAttivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item ? "Si" : "No"));
            }
        });

        tblListini.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedListino = newVal;
            if (newVal != null) {
                lblListinoSelezionato.setText(newVal.getNome());
                btnModificaListino.setDisable(false);
                loadPrezzi(newVal.getId());
            } else {
                lblListinoSelezionato.setText("Seleziona un listino");
                btnModificaListino.setDisable(true);
                tblPrezzi.getItems().clear();
            }
        });
    }

    private void setupPrezziTable() {
        colProdottoCodice.setCellValueFactory(cellData -> {
            Prodotto p = cellData.getValue().getProdotto();
            return new SimpleStringProperty(p != null ? p.getCodice() : "");
        });

        colProdottoNome.setCellValueFactory(cellData -> {
            Prodotto p = cellData.getValue().getProdotto();
            return new SimpleStringProperty(p != null ? p.getNome() : "");
        });

        colPrezzo.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        colPrezzo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : FormatUtil.formatCurrencyNoSymbol(item));
            }
        });

        colDataInizio.setCellValueFactory(cellData -> {
            LocalDate data = cellData.getValue().getDataInizio();
            return new SimpleStringProperty(data != null ? FormatUtil.formatDate(data) : "");
        });

        colDataFine.setCellValueFactory(cellData -> {
            LocalDate data = cellData.getValue().getDataFine();
            return new SimpleStringProperty(data != null ? FormatUtil.formatDate(data) : "");
        });

        // Double-click to edit price
        tblPrezzi.setRowFactory(tv -> {
            TableRow<ListinoPrezzo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modificaPrezzo(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadListini() {
        List<Listino> listini = listinoService.getAllListini();
        allListini.setAll(listini);
        tblListini.setItems(allListini);
    }

    private void loadPrezzi(int listinoId) {
        List<ListinoPrezzo> prezzi = listinoService.getPrezziByListino(listinoId);
        allPrezzi.setAll(prezzi);
        tblPrezzi.setItems(allPrezzi);
    }

    @FXML
    public void nuovoListino() {
        showListinoDialog(null);
    }

    @FXML
    public void modificaListino() {
        if (selectedListino != null) {
            showListinoDialog(selectedListino);
        }
    }

    private void showListinoDialog(Listino listino) {
        Dialog<Listino> dialog = new Dialog<>();
        dialog.setTitle(listino == null ? "Nuovo Listino" : "Modifica Listino");

        TextField txtCodice = new TextField();
        TextField txtNome = new TextField();
        TextArea txtDescrizione = new TextArea();
        txtDescrizione.setPrefRowCount(2);
        ComboBox<TipoListino> cmbTipo = new ComboBox<>();
        DatePicker dpInizio = new DatePicker();
        DatePicker dpFine = new DatePicker();
        CheckBox chkPrincipale = new CheckBox("Principale");
        CheckBox chkAttivo = new CheckBox("Attivo");

        cmbTipo.getItems().addAll(TipoListino.values());
        cmbTipo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(TipoListino t) {
                return t != null ? t.getDescrizione() : "";
            }

            @Override
            public TipoListino fromString(String s) {
                return null;
            }
        });

        if (listino != null) {
            txtCodice.setText(listino.getCodice());
            txtCodice.setDisable(true);
            txtNome.setText(listino.getNome());
            txtDescrizione.setText(listino.getDescrizione());
            cmbTipo.setValue(listino.getTipo());
            dpInizio.setValue(listino.getDataValiditaInizio());
            dpFine.setValue(listino.getDataValiditaFine());
            chkPrincipale.setSelected(listino.isPrincipale());
            chkAttivo.setSelected(listino.isAttivo());
        } else {
            cmbTipo.setValue(TipoListino.VENDITA);
            chkAttivo.setSelected(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Codice:"), 0, 0);
        grid.add(txtCodice, 1, 0);
        grid.add(new Label("Nome:"), 0, 1);
        grid.add(txtNome, 1, 1);
        grid.add(new Label("Tipo:"), 0, 2);
        grid.add(cmbTipo, 1, 2);
        grid.add(new Label("Descrizione:"), 0, 3);
        grid.add(txtDescrizione, 1, 3);
        grid.add(new Label("Validita' dal:"), 0, 4);
        grid.add(dpInizio, 1, 4);
        grid.add(new Label("Validita' al:"), 0, 5);
        grid.add(dpFine, 1, 5);
        grid.add(chkPrincipale, 1, 6);
        grid.add(chkAttivo, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Listino result = listino != null ? listino : new Listino();
                result.setCodice(txtCodice.getText().trim());
                result.setNome(txtNome.getText().trim());
                result.setDescrizione(txtDescrizione.getText());
                result.setTipo(cmbTipo.getValue());
                result.setDataValiditaInizio(dpInizio.getValue());
                result.setDataValiditaFine(dpFine.getValue());
                result.setPrincipale(chkPrincipale.isSelected());
                result.setAttivo(chkAttivo.isSelected());
                return result;
            }
            return null;
        });

        Optional<Listino> result = dialog.showAndWait();
        result.ifPresent(l -> {
            if (listinoService.saveListino(l) > 0) {
                AlertUtil.showInfo("Salvato", "Listino salvato correttamente");
                loadListini();
            } else {
                AlertUtil.showError("Errore", "Impossibile salvare il listino");
            }
        });
    }

    private void modificaPrezzo(ListinoPrezzo prezzo) {
        TextInputDialog dialog = new TextInputDialog(
                prezzo.getPrezzo() != null ? prezzo.getPrezzo().toString() : "");
        dialog.setTitle("Modifica Prezzo");
        dialog.setHeaderText("Prodotto: " + (prezzo.getProdotto() != null ? prezzo.getProdotto().getNome() : ""));
        dialog.setContentText("Nuovo prezzo:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(priceStr -> {
            try {
                BigDecimal newPrice = new BigDecimal(priceStr.replace(",", "."));
                prezzo.setPrezzo(newPrice);
                if (listinoService.savePrezzo(prezzo) > 0) {
                    loadPrezzi(selectedListino.getId());
                }
            } catch (NumberFormatException e) {
                AlertUtil.showError("Errore", "Prezzo non valido");
            }
        });
    }

    @FXML
    public void handleSearchPrezzi() {
        String searchText = txtSearchPrezzi.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            tblPrezzi.setItems(allPrezzi);
        } else {
            ObservableList<ListinoPrezzo> filtered = allPrezzi.filtered(p ->
                    (p.getProdotto() != null && (
                            p.getProdotto().getCodice().toLowerCase().contains(searchText) ||
                                    p.getProdotto().getNome().toLowerCase().contains(searchText))));
            tblPrezzi.setItems(filtered);
        }
    }

    @FXML
    public void importaCsv() {
        AlertUtil.showInfo("Import CSV", "Funzionalita' in sviluppo");
    }
}
