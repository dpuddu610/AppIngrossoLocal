package com.ingrosso.controller;

import com.ingrosso.model.*;
import com.ingrosso.service.MagazzinoService;
import com.ingrosso.service.ProdottoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class MovimentoDialogController {
    private static final Logger logger = LoggerFactory.getLogger(MovimentoDialogController.class);

    @FXML private ComboBox<TipoMovimento> cmbTipo;
    @FXML private ComboBox<Prodotto> cmbProdotto;
    @FXML private ComboBox<Magazzino> cmbMagazzino;
    @FXML private ComboBox<Magazzino> cmbMagazzinoDestinazione;
    @FXML private ComboBox<Lotto> cmbLotto;
    @FXML private TextField txtQuantita;
    @FXML private TextField txtCausale;
    @FXML private TextField txtDocumentoRif;
    @FXML private TextArea txtNote;
    @FXML private Label lblMagazzinoDestinazione;

    private final ProdottoService prodottoService = ProdottoService.getInstance();
    private final MagazzinoService magazzinoService = MagazzinoService.getInstance();

    private Movimento movimento;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupListeners();
    }

    private void setupComboBoxes() {
        // Tipo movimento
        cmbTipo.getItems().addAll(TipoMovimento.values());
        cmbTipo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(TipoMovimento t) {
                return t != null ? t.getDescrizione() : "";
            }

            @Override
            public TipoMovimento fromString(String s) {
                return null;
            }
        });

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

        // Magazzini
        List<Magazzino> magazzini = magazzinoService.getAllMagazzini();
        cmbMagazzino.getItems().addAll(magazzini);
        cmbMagazzinoDestinazione.getItems().addAll(magazzini);

        javafx.util.StringConverter<Magazzino> magConverter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(Magazzino m) {
                return m != null ? m.getNome() : "";
            }

            @Override
            public Magazzino fromString(String s) {
                return null;
            }
        };
        cmbMagazzino.setConverter(magConverter);
        cmbMagazzinoDestinazione.setConverter(magConverter);

        // Lotti
        cmbLotto.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Lotto l) {
                return l != null ? l.getNumeroLotto() : "";
            }

            @Override
            public Lotto fromString(String s) {
                return null;
            }
        });

        // Select magazzino principale
        magazzinoService.getMagazzinoPrincipale().ifPresent(cmbMagazzino::setValue);

        // Hide destinazione by default
        lblMagazzinoDestinazione.setVisible(false);
        cmbMagazzinoDestinazione.setVisible(false);
    }

    private void setupListeners() {
        // Show/hide destinazione based on tipo
        cmbTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isTrasferimento = newVal == TipoMovimento.TRASFERIMENTO;
            lblMagazzinoDestinazione.setVisible(isTrasferimento);
            cmbMagazzinoDestinazione.setVisible(isTrasferimento);
        });

        // Load lotti when product changes
        cmbProdotto.valueProperty().addListener((obs, oldVal, newVal) -> {
            cmbLotto.getItems().clear();
            if (newVal != null && newVal.isGestisceLotti()) {
                Magazzino mag = cmbMagazzino.getValue();
                if (mag != null) {
                    List<Lotto> lotti = magazzinoService.getLottiByProdotto(newVal.getId(), mag.getId());
                    cmbLotto.getItems().addAll(lotti);
                }
                cmbLotto.setDisable(false);
            } else {
                cmbLotto.setDisable(true);
            }
        });

        // Numeric validation
        txtQuantita.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtQuantita.setText(oldVal);
            }
        });
    }

    public void setMovimento(Movimento movimento) {
        this.movimento = movimento;

        if (movimento != null) {
            cmbTipo.setValue(movimento.getTipo());
            cmbMagazzino.setValue(movimento.getMagazzino());
            cmbProdotto.setValue(movimento.getProdotto());

            if (movimento.getQuantita() != null) {
                txtQuantita.setText(movimento.getQuantita().toString());
            }
            txtCausale.setText(movimento.getCausale());
            txtDocumentoRif.setText(movimento.getDocumentoRif());
            txtNote.setText(movimento.getNote());
        } else {
            cmbTipo.setValue(TipoMovimento.CARICO);
        }
    }

    public Movimento getMovimento() {
        if (!validateInput()) {
            return null;
        }

        if (movimento == null) {
            movimento = new Movimento();
        }

        movimento.setTipo(cmbTipo.getValue());
        movimento.setProdottoId(cmbProdotto.getValue().getId());
        movimento.setProdotto(cmbProdotto.getValue());
        movimento.setMagazzinoId(cmbMagazzino.getValue().getId());
        movimento.setMagazzino(cmbMagazzino.getValue());

        if (cmbLotto.getValue() != null) {
            movimento.setLottoId(cmbLotto.getValue().getId());
            movimento.setLotto(cmbLotto.getValue());
        }

        movimento.setQuantita(new BigDecimal(txtQuantita.getText().trim()));
        movimento.setCausale(txtCausale.getText().trim());
        movimento.setDocumentoRif(txtDocumentoRif.getText().trim());
        movimento.setNote(txtNote.getText());

        if (cmbTipo.getValue() == TipoMovimento.TRASFERIMENTO && cmbMagazzinoDestinazione.getValue() != null) {
            movimento.setMagazzinoDestinazioneId(cmbMagazzinoDestinazione.getValue().getId());
        }

        return movimento;
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (cmbTipo.getValue() == null) {
            errors.append("Selezionare il tipo di movimento\n");
        }

        if (cmbProdotto.getValue() == null) {
            errors.append("Selezionare un prodotto\n");
        }

        if (cmbMagazzino.getValue() == null) {
            errors.append("Selezionare un magazzino\n");
        }

        if (txtQuantita.getText().trim().isEmpty()) {
            errors.append("Inserire la quantita'\n");
        } else {
            try {
                BigDecimal qty = new BigDecimal(txtQuantita.getText().trim());
                if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.append("La quantita' deve essere positiva\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Quantita' non valida\n");
            }
        }

        if (cmbTipo.getValue() == TipoMovimento.TRASFERIMENTO) {
            if (cmbMagazzinoDestinazione.getValue() == null) {
                errors.append("Selezionare il magazzino di destinazione\n");
            } else if (cmbMagazzinoDestinazione.getValue().equals(cmbMagazzino.getValue())) {
                errors.append("Il magazzino di destinazione deve essere diverso da quello di origine\n");
            }
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
}
