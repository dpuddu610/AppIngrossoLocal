package com.ingrosso.controller;

import com.ingrosso.model.*;
import com.ingrosso.service.DdtService;
import com.ingrosso.util.AlertUtil;
import com.ingrosso.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DdtController {
    private static final Logger logger = LoggerFactory.getLogger(DdtController.class);

    @FXML private DatePicker dpDataInizio;
    @FXML private DatePicker dpDataFine;
    @FXML private ComboBox<Destinatario> cmbDestinatario;
    @FXML private ComboBox<StatoDdt> cmbStato;
    @FXML private TextField txtSearch;

    @FXML private TableView<Ddt> tblDdt;
    @FXML private TableColumn<Ddt, String> colNumero;
    @FXML private TableColumn<Ddt, String> colData;
    @FXML private TableColumn<Ddt, String> colDestinatario;
    @FXML private TableColumn<Ddt, String> colStato;
    @FXML private TableColumn<Ddt, Integer> colRighe;
    @FXML private TableColumn<Ddt, Void> colAzioni;

    @FXML private Label lblTotaleDdt;

    private final DdtService ddtService = DdtService.getInstance();

    private ObservableList<Ddt> allDdt = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        setupDatePickers();
        loadData();
    }

    private void setupComboBoxes() {
        // Destinatari
        List<Destinatario> destinatari = ddtService.getAllDestinatari();
        cmbDestinatario.getItems().add(null);
        cmbDestinatario.getItems().addAll(destinatari);
        cmbDestinatario.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Destinatario d) {
                return d != null ? d.getRagioneSociale() : "Tutti i destinatari";
            }

            @Override
            public Destinatario fromString(String s) {
                return null;
            }
        });

        // Stati
        cmbStato.getItems().add(null);
        cmbStato.getItems().addAll(StatoDdt.values());
        cmbStato.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(StatoDdt s) {
                return s != null ? s.getDescrizione() : "Tutti gli stati";
            }

            @Override
            public StatoDdt fromString(String s) {
                return null;
            }
        });
    }

    private void setupTable() {
        colNumero.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNumeroCompleto()));

        colData.setCellValueFactory(cellData -> {
            LocalDate data = cellData.getValue().getDataDocumento();
            return new SimpleStringProperty(data != null ? FormatUtil.formatDate(data) : "");
        });

        colDestinatario.setCellValueFactory(cellData -> {
            Destinatario dest = cellData.getValue().getDestinatario();
            return new SimpleStringProperty(dest != null ? dest.getRagioneSociale() : "");
        });

        colStato.setCellValueFactory(cellData -> {
            StatoDdt stato = cellData.getValue().getStato();
            return new SimpleStringProperty(stato != null ? stato.getDescrizione() : "");
        });

        colRighe.setCellValueFactory(new PropertyValueFactory<>("totaleRighe"));

        // Action buttons
        colAzioni.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("Apri");
            private final Button btnPrint = new Button("PDF");
            private final HBox container = new HBox(4, btnView, btnPrint);

            {
                btnView.getStyleClass().add("button-small");
                btnPrint.getStyleClass().addAll("button-small", "button-outline");

                btnView.setOnAction(e -> {
                    Ddt ddt = getTableView().getItems().get(getIndex());
                    apriDdt(ddt);
                });

                btnPrint.setOnAction(e -> {
                    Ddt ddt = getTableView().getItems().get(getIndex());
                    stampaDdt(ddt);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Double-click to open
        tblDdt.setRowFactory(tv -> {
            TableRow<Ddt> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    apriDdt(row.getItem());
                }
            });
            return row;
        });

        // Color code by status
        tblDdt.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Ddt item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    switch (item.getStato()) {
                        case BOZZA -> setStyle("-fx-background-color: #FEF9C3;");
                        case EMESSO -> setStyle("-fx-background-color: #DCFCE7;");
                        case ANNULLATO -> setStyle("-fx-background-color: #FEE2E2;");
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    private void setupDatePickers() {
        LocalDate today = LocalDate.now();
        dpDataFine.setValue(today);
        dpDataInizio.setValue(today.withDayOfMonth(1));
    }

    private void loadData() {
        LocalDate dataInizio = dpDataInizio.getValue();
        LocalDate dataFine = dpDataFine.getValue();

        List<Ddt> ddtList = ddtService.getDdtByPeriodo(dataInizio, dataFine);

        // Load righe for each DDT
        for (Ddt ddt : ddtList) {
            ddtService.loadRighe(ddt);
            ddtService.getDdtById(ddt.getId()).ifPresent(fullDdt -> {
                ddt.setDestinatario(fullDdt.getDestinatario());
            });
        }

        allDdt.setAll(ddtList);
        applyFilters();
    }

    @FXML
    public void handleFilter() {
        applyFilters();
        loadData();
    }

    private void applyFilters() {
        Destinatario destinatario = cmbDestinatario.getValue();
        StatoDdt stato = cmbStato.getValue();
        String searchText = txtSearch.getText().toLowerCase().trim();

        ObservableList<Ddt> filtered = allDdt.filtered(ddt -> {
            if (destinatario != null && (ddt.getDestinatario() == null ||
                    ddt.getDestinatarioId() != destinatario.getId())) {
                return false;
            }

            if (stato != null && ddt.getStato() != stato) {
                return false;
            }

            if (!searchText.isEmpty()) {
                String numero = String.valueOf(ddt.getNumero());
                String dest = ddt.getDestinatario() != null ?
                        ddt.getDestinatario().getRagioneSociale().toLowerCase() : "";
                if (!numero.contains(searchText) && !dest.contains(searchText)) {
                    return false;
                }
            }

            return true;
        });

        tblDdt.setItems(filtered);
        updateTotals();
    }

    private void updateTotals() {
        lblTotaleDdt.setText(String.valueOf(tblDdt.getItems().size()));
    }

    @FXML
    public void nuovoDdt() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ddt-dettaglio.fxml"));
            Parent content = loader.load();

            DdtDettaglioController controller = loader.getController();
            controller.setDdt(null);
            controller.setOnSave(() -> loadData());

            // Replace content in main area
            tblDdt.getScene().lookup("#contentArea");
            // For now, just open as dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nuovo DDT");
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            dialog.showAndWait();
            loadData();

        } catch (IOException e) {
            logger.error("Error opening DDT detail: {}", e.getMessage());
            AlertUtil.showError("Errore", "Impossibile aprire il dettaglio DDT");
        }
    }

    private void apriDdt(Ddt ddt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ddt-dettaglio.fxml"));
            Parent content = loader.load();

            DdtDettaglioController controller = loader.getController();
            controller.setDdt(ddt);
            controller.setOnSave(() -> loadData());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("DDT " + ddt.getNumeroCompleto());
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            dialog.showAndWait();
            loadData();

        } catch (IOException e) {
            logger.error("Error opening DDT detail: {}", e.getMessage());
            AlertUtil.showError("Errore", "Impossibile aprire il dettaglio DDT");
        }
    }

    private void stampaDdt(Ddt ddt) {
        byte[] pdfData = ddtService.generatePdf(ddt);
        if (pdfData != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salva DDT PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("DDT_" + ddt.getNumero() + "_" + ddt.getAnno() + ".pdf");

            File file = fileChooser.showSaveDialog(tblDdt.getScene().getWindow());
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(pdfData);
                    AlertUtil.showInfo("PDF generato", "File salvato in " + file.getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Error saving PDF: {}", e.getMessage());
                    AlertUtil.showError("Errore", "Impossibile salvare il PDF");
                }
            }
        }
    }

    @FXML
    public void refreshData() {
        loadData();
    }
}
