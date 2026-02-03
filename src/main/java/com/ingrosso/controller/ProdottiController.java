package com.ingrosso.controller;

import com.ingrosso.model.*;
import com.ingrosso.service.MagazzinoService;
import com.ingrosso.service.ProdottoService;
import com.ingrosso.util.AlertUtil;
import com.ingrosso.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProdottiController {
    private static final Logger logger = LoggerFactory.getLogger(ProdottiController.class);

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Sottocategoria> cmbSottocategoria;
    @FXML private CheckBox chkSoloAttivi;
    @FXML private CheckBox chkSottoScorta;

    @FXML private TableView<Prodotto> tblProdotti;
    @FXML private TableColumn<Prodotto, String> colCodice;
    @FXML private TableColumn<Prodotto, String> colBarcode;
    @FXML private TableColumn<Prodotto, String> colNome;
    @FXML private TableColumn<Prodotto, String> colCategoria;
    @FXML private TableColumn<Prodotto, String> colUM;
    @FXML private TableColumn<Prodotto, BigDecimal> colGiacenza;
    @FXML private TableColumn<Prodotto, BigDecimal> colPrezzoAcquisto;
    @FXML private TableColumn<Prodotto, BigDecimal> colPrezzoVendita;
    @FXML private TableColumn<Prodotto, Void> colAzioni;

    @FXML private Label lblStatus;

    private final ProdottoService prodottoService = ProdottoService.getInstance();
    private final MagazzinoService magazzinoService = MagazzinoService.getInstance();

    private ObservableList<Prodotto> allProdotti = FXCollections.observableArrayList();
    private FilteredList<Prodotto> filteredProdotti;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        colCodice.setCellValueFactory(new PropertyValueFactory<>("codice"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        colCategoria.setCellValueFactory(cellData -> {
            Prodotto p = cellData.getValue();
            Sottocategoria sc = p.getSottocategoria();
            if (sc != null && sc.getCategoria() != null) {
                return new SimpleStringProperty(sc.getCategoria().getNome() + " / " + sc.getNome());
            } else if (sc != null) {
                return new SimpleStringProperty(sc.getNome());
            }
            return new SimpleStringProperty("");
        });

        colUM.setCellValueFactory(cellData -> {
            UnitaMisura um = cellData.getValue().getUnitaMisura();
            return new SimpleStringProperty(um != null ? um.getSimbolo() : "");
        });

        colGiacenza.setCellValueFactory(cellData -> {
            Prodotto p = cellData.getValue();
            BigDecimal giacenza = magazzinoService.getGiacenzaTotale(p.getId());
            return new javafx.beans.property.SimpleObjectProperty<>(giacenza);
        });

        colPrezzoAcquisto.setCellValueFactory(new PropertyValueFactory<>("prezzoAcquisto"));
        colPrezzoAcquisto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : FormatUtil.formatCurrencyNoSymbol(item));
            }
        });

        colPrezzoVendita.setCellValueFactory(new PropertyValueFactory<>("prezzoVendita"));
        colPrezzoVendita.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : FormatUtil.formatCurrencyNoSymbol(item));
            }
        });

        // Action buttons column
        colAzioni.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifica");
            private final Button btnDelete = new Button("X");
            private final HBox container = new HBox(4, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("button-small");
                btnDelete.getStyleClass().addAll("button-small", "button-danger");

                btnEdit.setOnAction(e -> {
                    Prodotto prodotto = getTableView().getItems().get(getIndex());
                    modificaProdotto(prodotto);
                });

                btnDelete.setOnAction(e -> {
                    Prodotto prodotto = getTableView().getItems().get(getIndex());
                    eliminaProdotto(prodotto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Row double-click to edit
        tblProdotti.setRowFactory(tv -> {
            TableRow<Prodotto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modificaProdotto(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        // Load categories
        List<Categoria> categorie = prodottoService.getAllCategorie();
        cmbCategoria.getItems().add(null); // "All" option
        cmbCategoria.getItems().addAll(categorie);

        cmbCategoria.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categoria c) {
                return c == null ? "Tutte le categorie" : c.getNome();
            }

            @Override
            public Categoria fromString(String s) {
                return null;
            }
        });

        cmbSottocategoria.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Sottocategoria s) {
                return s == null ? "Tutte le sottocategorie" : s.getNome();
            }

            @Override
            public Sottocategoria fromString(String s) {
                return null;
            }
        });

        // Category selection updates subcategories
        cmbCategoria.valueProperty().addListener((obs, oldVal, newVal) -> {
            cmbSottocategoria.getItems().clear();
            cmbSottocategoria.getItems().add(null);
            if (newVal != null) {
                List<Sottocategoria> sottocategorie = prodottoService.getSottocategorieByCategoria(newVal.getId());
                cmbSottocategoria.getItems().addAll(sottocategorie);
            }
        });
    }

    private void loadData() {
        List<Prodotto> prodotti = chkSoloAttivi.isSelected() ?
                prodottoService.getAllProdottiAttivi() :
                prodottoService.getAllProdotti();

        allProdotti.setAll(prodotti);
        filteredProdotti = new FilteredList<>(allProdotti, p -> true);
        tblProdotti.setItems(filteredProdotti);

        updateStatus();
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
        Categoria categoria = cmbCategoria.getValue();
        Sottocategoria sottocategoria = cmbSottocategoria.getValue();
        boolean soloAttivi = chkSoloAttivi.isSelected();
        boolean sottoScorta = chkSottoScorta.isSelected();

        filteredProdotti.setPredicate(prodotto -> {
            // Text search
            if (!searchText.isEmpty()) {
                boolean matches = prodotto.getCodice().toLowerCase().contains(searchText) ||
                        prodotto.getNome().toLowerCase().contains(searchText) ||
                        (prodotto.getBarcode() != null && prodotto.getBarcode().toLowerCase().contains(searchText));
                if (!matches) return false;
            }

            // Category filter
            if (categoria != null) {
                Sottocategoria sc = prodotto.getSottocategoria();
                if (sc == null || sc.getCategoriaId() != categoria.getId()) return false;
            }

            // Subcategory filter
            if (sottocategoria != null) {
                if (prodotto.getSottocategoriaId() != sottocategoria.getId()) return false;
            }

            // Active filter
            if (soloAttivi && !prodotto.isAttivo()) return false;

            // Sotto scorta filter
            if (sottoScorta) {
                BigDecimal giacenza = magazzinoService.getGiacenzaTotale(prodotto.getId());
                if (giacenza.compareTo(prodotto.getScortaMinima()) >= 0) return false;
            }

            return true;
        });

        updateStatus();
    }

    private void updateStatus() {
        int visible = filteredProdotti.size();
        int total = allProdotti.size();
        lblStatus.setText(String.format("Visualizzati %d di %d prodotti", visible, total));
    }

    @FXML
    public void nuovoProdotto() {
        showProdottoDialog(null);
    }

    private void modificaProdotto(Prodotto prodotto) {
        showProdottoDialog(prodotto);
    }

    private void showProdottoDialog(Prodotto prodotto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prodotto-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            ProdottoDialogController controller = loader.getController();
            controller.setProdotto(prodotto);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(prodotto == null ? "Nuovo Prodotto" : "Modifica Prodotto");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Prodotto edited = controller.getProdotto();
                if (edited != null) {
                    prodottoService.saveProdotto(edited);
                    loadData();
                }
            }
        } catch (IOException e) {
            logger.error("Error opening prodotto dialog: {}", e.getMessage());
            AlertUtil.showError("Errore", "Impossibile aprire il dialog");
        }
    }

    private void eliminaProdotto(Prodotto prodotto) {
        Optional<ButtonType> result = AlertUtil.showConfirmation(
                "Conferma eliminazione",
                "Sei sicuro di voler eliminare il prodotto " + prodotto.getNome() + "?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Soft delete - set attivo = false
            prodotto.setAttivo(false);
            prodottoService.saveProdotto(prodotto);
            loadData();
        }
    }

    @FXML
    public void esportaCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Esporta Prodotti");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("prodotti.csv");

        File file = fileChooser.showSaveDialog(tblProdotti.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Header
                writer.write("Codice;Barcode;Nome;Categoria;U.M.;Prezzo Acquisto;Prezzo Vendita;Scorta Minima\n");

                // Data
                for (Prodotto p : filteredProdotti) {
                    writer.write(String.format("%s;%s;%s;%s;%s;%s;%s;%s\n",
                            p.getCodice(),
                            p.getBarcode() != null ? p.getBarcode() : "",
                            p.getNome(),
                            p.getSottocategoria() != null ? p.getSottocategoria().getNome() : "",
                            p.getUnitaMisura() != null ? p.getUnitaMisura().getSimbolo() : "",
                            p.getPrezzoAcquisto() != null ? p.getPrezzoAcquisto().toString() : "",
                            p.getPrezzoVendita() != null ? p.getPrezzoVendita().toString() : "",
                            p.getScortaMinima() != null ? p.getScortaMinima().toString() : ""));
                }

                AlertUtil.showInfo("Export completato", "File salvato in " + file.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Error exporting CSV: {}", e.getMessage());
                AlertUtil.showError("Errore", "Impossibile esportare il file");
            }
        }
    }
}
