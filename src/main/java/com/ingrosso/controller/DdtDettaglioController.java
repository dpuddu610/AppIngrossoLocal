package com.ingrosso.controller;

import com.ingrosso.model.*;
import com.ingrosso.service.DdtService;
import com.ingrosso.service.MagazzinoService;
import com.ingrosso.service.ProdottoService;
import com.ingrosso.util.AlertUtil;
import com.ingrosso.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DdtDettaglioController {
    private static final Logger logger = LoggerFactory.getLogger(DdtDettaglioController.class);

    // Header fields
    @FXML private Label lblNumero;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<Destinatario> cmbDestinatario;
    @FXML private TextField txtDestinazioneDiversa;
    @FXML private ComboBox<Magazzino> cmbMagazzino;
    @FXML private TextField txtCausale;
    @FXML private TextField txtAspettoBeni;
    @FXML private TextField txtColli;
    @FXML private TextField txtPeso;
    @FXML private TextField txtPorto;
    @FXML private TextField txtVettore;
    @FXML private TextArea txtNote;
    @FXML private Label lblStato;

    // Righe table
    @FXML private TableView<DdtRiga> tblRighe;
    @FXML private TableColumn<DdtRiga, String> colProdotto;
    @FXML private TableColumn<DdtRiga, String> colDescrizione;
    @FXML private TableColumn<DdtRiga, BigDecimal> colQuantita;
    @FXML private TableColumn<DdtRiga, String> colUM;
    @FXML private TableColumn<DdtRiga, BigDecimal> colPrezzo;
    @FXML private TableColumn<DdtRiga, Void> colAzioni;

    // Add row fields
    @FXML private ComboBox<Prodotto> cmbProdotto;
    @FXML private TextField txtQuantitaRiga;
    @FXML private TextField txtPrezzoRiga;

    // Buttons
    @FXML private Button btnSalva;
    @FXML private Button btnEmetti;
    @FXML private Button btnAnnulla;

    private final DdtService ddtService = DdtService.getInstance();
    private final MagazzinoService magazzinoService = MagazzinoService.getInstance();
    private final ProdottoService prodottoService = ProdottoService.getInstance();

    private Ddt ddt;
    private Runnable onSave;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        setupValidation();
    }

    private void setupComboBoxes() {
        // Destinatari
        List<Destinatario> destinatari = ddtService.getAllDestinatari();
        cmbDestinatario.getItems().addAll(destinatari);
        cmbDestinatario.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Destinatario d) {
                return d != null ? d.getRagioneSociale() : "";
            }

            @Override
            public Destinatario fromString(String s) {
                return null;
            }
        });

        // Magazzini
        List<Magazzino> magazzini = magazzinoService.getAllMagazzini();
        cmbMagazzino.getItems().addAll(magazzini);
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
        magazzinoService.getMagazzinoPrincipale().ifPresent(cmbMagazzino::setValue);

        // Prodotti
        List<Prodotto> prodotti = prodottoService.getAllProdottiAttivi();
        cmbProdotto.getItems().addAll(prodotti);
        cmbProdotto.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Prodotto p) {
                return p != null ? p.getCodice() + " - " + p.getNome() : "";
            }

            @Override
            public Prodotto fromString(String s) {
                return null;
            }
        });
    }

    private void setupTable() {
        colProdotto.setCellValueFactory(cellData -> {
            Prodotto p = cellData.getValue().getProdotto();
            return new SimpleStringProperty(p != null ? p.getCodice() : "");
        });

        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colQuantita.setCellValueFactory(new PropertyValueFactory<>("quantita"));
        colUM.setCellValueFactory(new PropertyValueFactory<>("unitaMisura"));

        colPrezzo.setCellValueFactory(new PropertyValueFactory<>("prezzoUnitario"));
        colPrezzo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : FormatUtil.formatCurrencyNoSymbol(item));
            }
        });

        colAzioni.setCellFactory(col -> new TableCell<>() {
            private final Button btnDelete = new Button("X");

            {
                btnDelete.getStyleClass().addAll("button-small", "button-danger");
                btnDelete.setOnAction(e -> {
                    DdtRiga riga = getTableView().getItems().get(getIndex());
                    rimuoviRiga(riga);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || ddt == null || ddt.getStato() != StatoDdt.BOZZA) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDelete);
                }
            }
        });
    }

    private void setupValidation() {
        txtQuantitaRiga.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtQuantitaRiga.setText(oldVal);
            }
        });

        txtPrezzoRiga.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPrezzoRiga.setText(oldVal);
            }
        });

        txtColli.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtColli.setText(oldVal);
            }
        });

        txtPeso.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPeso.setText(oldVal);
            }
        });
    }

    public void setDdt(Ddt ddt) {
        if (ddt != null) {
            this.ddt = ddt;
            ddtService.loadRighe(ddt);
            populateForm();
        } else {
            this.ddt = new Ddt();
            this.ddt.setDataDocumento(LocalDate.now());
            int nextNumero = ddtService.getNextNumero(LocalDate.now().getYear());
            this.ddt.setNumero(nextNumero);
            lblNumero.setText(this.ddt.getNumeroCompleto());
            dpData.setValue(LocalDate.now());
            lblStato.setText("Bozza");
        }
        updateEditability();
    }

    private void populateForm() {
        lblNumero.setText(ddt.getNumeroCompleto());
        dpData.setValue(ddt.getDataDocumento());
        cmbDestinatario.setValue(ddt.getDestinatario());
        txtDestinazioneDiversa.setText(ddt.getDestinazioneDiversa());
        cmbMagazzino.setValue(ddt.getMagazzino());
        txtCausale.setText(ddt.getCausaleTrasporto());
        txtAspettoBeni.setText(ddt.getAspettoBeni());
        if (ddt.getColli() > 0) txtColli.setText(String.valueOf(ddt.getColli()));
        if (ddt.getPesoKg() != null) txtPeso.setText(ddt.getPesoKg().toString());
        txtPorto.setText(ddt.getPorto());
        txtVettore.setText(ddt.getVettore());
        txtNote.setText(ddt.getNote());
        lblStato.setText(ddt.getStato().getDescrizione());

        tblRighe.getItems().setAll(ddt.getRighe());
    }

    private void updateEditability() {
        boolean editable = ddt == null || ddt.getStato() == StatoDdt.BOZZA;

        dpData.setDisable(!editable);
        cmbDestinatario.setDisable(!editable);
        txtDestinazioneDiversa.setDisable(!editable);
        cmbMagazzino.setDisable(!editable);
        txtCausale.setDisable(!editable);
        txtAspettoBeni.setDisable(!editable);
        txtColli.setDisable(!editable);
        txtPeso.setDisable(!editable);
        txtPorto.setDisable(!editable);
        txtVettore.setDisable(!editable);
        txtNote.setDisable(!editable);

        cmbProdotto.setDisable(!editable);
        txtQuantitaRiga.setDisable(!editable);
        txtPrezzoRiga.setDisable(!editable);

        btnSalva.setDisable(!editable);
        btnEmetti.setDisable(!editable);
        btnAnnulla.setDisable(ddt == null || ddt.getStato() != StatoDdt.EMESSO);
    }

    @FXML
    public void aggiungiRiga() {
        Prodotto prodotto = cmbProdotto.getValue();
        if (prodotto == null) {
            AlertUtil.showWarning("Attenzione", "Selezionare un prodotto");
            return;
        }

        if (txtQuantitaRiga.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Attenzione", "Inserire la quantita'");
            return;
        }

        DdtRiga riga = new DdtRiga();
        riga.setProdottoId(prodotto.getId());
        riga.setProdotto(prodotto);
        riga.setDescrizione(prodotto.getNome());
        riga.setQuantita(new BigDecimal(txtQuantitaRiga.getText().trim()));
        riga.setUnitaMisura(prodotto.getUnitaMisura() != null ? prodotto.getUnitaMisura().getSimbolo() : "");

        if (!txtPrezzoRiga.getText().trim().isEmpty()) {
            riga.setPrezzoUnitario(new BigDecimal(txtPrezzoRiga.getText().trim()));
        } else if (prodotto.getPrezzoVendita() != null) {
            riga.setPrezzoUnitario(prodotto.getPrezzoVendita());
        }

        riga.setAliquotaIva(prodotto.getAliquotaIva());
        riga.setOrdine(tblRighe.getItems().size() + 1);

        tblRighe.getItems().add(riga);
        ddt.getRighe().add(riga);

        // Clear inputs
        cmbProdotto.setValue(null);
        txtQuantitaRiga.clear();
        txtPrezzoRiga.clear();
    }

    private void rimuoviRiga(DdtRiga riga) {
        tblRighe.getItems().remove(riga);
        ddt.getRighe().remove(riga);
    }

    @FXML
    public void salvaBozza() {
        if (!validateDdt()) return;

        updateDdtFromForm();
        ddt.setStato(StatoDdt.BOZZA);

        int id = ddtService.saveDdt(ddt);
        if (id > 0) {
            ddt.setId(id);
            AlertUtil.showInfo("Salvato", "DDT salvato come bozza");
            if (onSave != null) onSave.run();
        } else {
            AlertUtil.showError("Errore", "Impossibile salvare il DDT");
        }
    }

    @FXML
    public void emettiDdt() {
        if (!validateDdt()) return;

        if (ddt.getRighe().isEmpty()) {
            AlertUtil.showWarning("Attenzione", "Aggiungere almeno una riga");
            return;
        }

        Optional<ButtonType> result = AlertUtil.showConfirmation(
                "Conferma emissione",
                "Emettendo il DDT verra' scaricata la merce dal magazzino. Continuare?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateDdtFromForm();

            if (ddtService.emettiDdt(ddt)) {
                AlertUtil.showInfo("Emesso", "DDT emesso correttamente");
                updateEditability();
                lblStato.setText(StatoDdt.EMESSO.getDescrizione());
                if (onSave != null) onSave.run();
            } else {
                AlertUtil.showError("Errore", "Impossibile emettere il DDT");
            }
        }
    }

    @FXML
    public void annullaDdt() {
        Optional<ButtonType> result = AlertUtil.showConfirmation(
                "Conferma annullamento",
                "Sei sicuro di voler annullare questo DDT?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (ddtService.annullaDdt(ddt.getId())) {
                ddt.setStato(StatoDdt.ANNULLATO);
                AlertUtil.showInfo("Annullato", "DDT annullato");
                updateEditability();
                lblStato.setText(StatoDdt.ANNULLATO.getDescrizione());
                if (onSave != null) onSave.run();
            } else {
                AlertUtil.showError("Errore", "Impossibile annullare il DDT");
            }
        }
    }

    private void updateDdtFromForm() {
        ddt.setDataDocumento(dpData.getValue());
        if (cmbDestinatario.getValue() != null) {
            ddt.setDestinatarioId(cmbDestinatario.getValue().getId());
            ddt.setDestinatario(cmbDestinatario.getValue());
        }
        ddt.setDestinazioneDiversa(txtDestinazioneDiversa.getText());
        if (cmbMagazzino.getValue() != null) {
            ddt.setMagazzinoId(cmbMagazzino.getValue().getId());
            ddt.setMagazzino(cmbMagazzino.getValue());
        }
        ddt.setCausaleTrasporto(txtCausale.getText());
        ddt.setAspettoBeni(txtAspettoBeni.getText());
        if (!txtColli.getText().isEmpty()) {
            ddt.setColli(Integer.parseInt(txtColli.getText()));
        }
        if (!txtPeso.getText().isEmpty()) {
            ddt.setPesoKg(new BigDecimal(txtPeso.getText()));
        }
        ddt.setPorto(txtPorto.getText());
        ddt.setVettore(txtVettore.getText());
        ddt.setNote(txtNote.getText());
    }

    private boolean validateDdt() {
        if (cmbMagazzino.getValue() == null) {
            AlertUtil.showWarning("Attenzione", "Selezionare il magazzino");
            return false;
        }
        return true;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}
