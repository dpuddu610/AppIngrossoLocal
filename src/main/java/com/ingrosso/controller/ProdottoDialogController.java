package com.ingrosso.controller;

import com.ingrosso.model.*;
import com.ingrosso.service.ProdottoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class ProdottoDialogController {
    private static final Logger logger = LoggerFactory.getLogger(ProdottoDialogController.class);

    @FXML private TextField txtCodice;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescrizione;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Sottocategoria> cmbSottocategoria;
    @FXML private ComboBox<UnitaMisura> cmbUnitaMisura;
    @FXML private TextField txtPrezzoAcquisto;
    @FXML private TextField txtPrezzoVendita;
    @FXML private TextField txtAliquotaIva;
    @FXML private TextField txtScortaMinima;
    @FXML private TextField txtScortaMassima;
    @FXML private CheckBox chkGestisceLotti;
    @FXML private CheckBox chkAttivo;
    @FXML private TextArea txtNote;

    private final ProdottoService prodottoService = ProdottoService.getInstance();
    private Prodotto prodotto;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupValidation();
    }

    private void setupComboBoxes() {
        // Categorie
        List<Categoria> categorie = prodottoService.getAllCategorie();
        cmbCategoria.getItems().addAll(categorie);
        cmbCategoria.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categoria c) {
                return c != null ? c.getNome() : "";
            }

            @Override
            public Categoria fromString(String s) {
                return null;
            }
        });

        // Sottocategorie cascade
        cmbSottocategoria.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Sottocategoria s) {
                return s != null ? s.getNome() : "";
            }

            @Override
            public Sottocategoria fromString(String s) {
                return null;
            }
        });

        cmbCategoria.valueProperty().addListener((obs, oldVal, newVal) -> {
            cmbSottocategoria.getItems().clear();
            if (newVal != null) {
                List<Sottocategoria> sottocategorie = prodottoService.getSottocategorieByCategoria(newVal.getId());
                cmbSottocategoria.getItems().addAll(sottocategorie);
            }
        });

        // Unita di misura
        List<UnitaMisura> unitaMisura = prodottoService.getAllUnitaMisura();
        cmbUnitaMisura.getItems().addAll(unitaMisura);
        cmbUnitaMisura.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(UnitaMisura um) {
                return um != null ? um.getNome() + " (" + um.getSimbolo() + ")" : "";
            }

            @Override
            public UnitaMisura fromString(String s) {
                return null;
            }
        });
    }

    private void setupValidation() {
        // Numeric validation for price fields
        txtPrezzoAcquisto.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPrezzoAcquisto.setText(oldVal);
            }
        });

        txtPrezzoVendita.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtPrezzoVendita.setText(oldVal);
            }
        });

        txtAliquotaIva.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtAliquotaIva.setText(oldVal);
            }
        });

        txtScortaMinima.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtScortaMinima.setText(oldVal);
            }
        });

        txtScortaMassima.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtScortaMassima.setText(oldVal);
            }
        });
    }

    public void setProdotto(Prodotto prodotto) {
        this.prodotto = prodotto;

        if (prodotto != null) {
            // Editing existing product
            txtCodice.setText(prodotto.getCodice());
            txtCodice.setDisable(true); // Don't allow code change
            txtBarcode.setText(prodotto.getBarcode());
            txtNome.setText(prodotto.getNome());
            txtDescrizione.setText(prodotto.getDescrizione());

            // Set category and subcategory
            if (prodotto.getSottocategoria() != null) {
                Categoria cat = prodotto.getSottocategoria().getCategoria();
                cmbCategoria.setValue(cat);
                // Trigger subcategory load
                if (cat != null) {
                    List<Sottocategoria> sottocategorie = prodottoService.getSottocategorieByCategoria(cat.getId());
                    cmbSottocategoria.getItems().setAll(sottocategorie);
                    cmbSottocategoria.setValue(prodotto.getSottocategoria());
                }
            }

            cmbUnitaMisura.setValue(prodotto.getUnitaMisura());

            if (prodotto.getPrezzoAcquisto() != null) {
                txtPrezzoAcquisto.setText(prodotto.getPrezzoAcquisto().toString());
            }
            if (prodotto.getPrezzoVendita() != null) {
                txtPrezzoVendita.setText(prodotto.getPrezzoVendita().toString());
            }
            if (prodotto.getAliquotaIva() != null) {
                txtAliquotaIva.setText(prodotto.getAliquotaIva().toString());
            }
            if (prodotto.getScortaMinima() != null) {
                txtScortaMinima.setText(prodotto.getScortaMinima().toString());
            }
            if (prodotto.getScortaMassima() != null) {
                txtScortaMassima.setText(prodotto.getScortaMassima().toString());
            }

            chkGestisceLotti.setSelected(prodotto.isGestisceLotti());
            chkAttivo.setSelected(prodotto.isAttivo());
            txtNote.setText(prodotto.getNote());
        } else {
            // New product - generate code
            txtCodice.setText(prodottoService.generateNextCodice());
            txtAliquotaIva.setText("22.00");
            chkAttivo.setSelected(true);
        }
    }

    public Prodotto getProdotto() {
        if (!validateInput()) {
            return null;
        }

        if (prodotto == null) {
            prodotto = new Prodotto();
        }

        prodotto.setCodice(txtCodice.getText().trim());
        prodotto.setBarcode(txtBarcode.getText().trim().isEmpty() ? null : txtBarcode.getText().trim());
        prodotto.setNome(txtNome.getText().trim());
        prodotto.setDescrizione(txtDescrizione.getText());

        Sottocategoria sc = cmbSottocategoria.getValue();
        if (sc != null) {
            prodotto.setSottocategoriaId(sc.getId());
            prodotto.setSottocategoria(sc);
        }

        UnitaMisura um = cmbUnitaMisura.getValue();
        if (um != null) {
            prodotto.setUnitaMisuraId(um.getId());
            prodotto.setUnitaMisura(um);
        }

        prodotto.setPrezzoAcquisto(parseDecimal(txtPrezzoAcquisto.getText()));
        prodotto.setPrezzoVendita(parseDecimal(txtPrezzoVendita.getText()));
        prodotto.setAliquotaIva(parseDecimal(txtAliquotaIva.getText()));
        prodotto.setScortaMinima(parseDecimal(txtScortaMinima.getText()));
        prodotto.setScortaMassima(parseDecimal(txtScortaMassima.getText()));

        prodotto.setGestisceLotti(chkGestisceLotti.isSelected());
        prodotto.setAttivo(chkAttivo.isSelected());
        prodotto.setNote(txtNote.getText());

        return prodotto;
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtCodice.getText().trim().isEmpty()) {
            errors.append("Il codice e' obbligatorio\n");
        }

        if (txtNome.getText().trim().isEmpty()) {
            errors.append("Il nome e' obbligatorio\n");
        }

        if (cmbUnitaMisura.getValue() == null) {
            errors.append("L'unita' di misura e' obbligatoria\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore di validazione");
            alert.setHeaderText("Correggere i seguenti errori:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private BigDecimal parseDecimal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    public void generaCodice() {
        txtCodice.setText(prodottoService.generateNextCodice());
    }
}
