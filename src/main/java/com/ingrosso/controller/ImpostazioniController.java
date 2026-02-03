package com.ingrosso.controller;

import com.ingrosso.model.*;
import com.ingrosso.service.AuthService;
import com.ingrosso.service.DatabaseService;
import com.ingrosso.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class ImpostazioniController {
    private static final Logger logger = LoggerFactory.getLogger(ImpostazioniController.class);

    // Azienda fields
    @FXML private TextField txtNomeAzienda;
    @FXML private TextField txtIndirizzo;
    @FXML private TextField txtCitta;
    @FXML private TextField txtCap;
    @FXML private TextField txtProvincia;
    @FXML private TextField txtPiva;
    @FXML private TextField txtCodiceFiscale;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

    // Database fields
    @FXML private TextField txtDbHost;
    @FXML private TextField txtDbPort;
    @FXML private TextField txtDbName;
    @FXML private TextField txtDbUser;
    @FXML private PasswordField txtDbPassword;

    // Utenti table
    @FXML private TableView<Utente> tblUtenti;
    @FXML private TableColumn<Utente, String> colUsername;
    @FXML private TableColumn<Utente, String> colNome;
    @FXML private TableColumn<Utente, String> colCognome;
    @FXML private TableColumn<Utente, String> colRuolo;
    @FXML private TableColumn<Utente, Boolean> colAttivo;
    @FXML private TableColumn<Utente, Void> colAzioni;

    private final AuthService authService = AuthService.getInstance();
    private final DatabaseService databaseService = DatabaseService.getInstance();

    @FXML
    public void initialize() {
        loadAziendaData();
        loadDatabaseConfig();
        setupUtentiTable();
        loadUtenti();
    }

    private void loadAziendaData() {
        authService.getConfigAzienda().ifPresent(config -> {
            txtNomeAzienda.setText(config.getNome());
            txtIndirizzo.setText(config.getIndirizzo());
            txtCitta.setText(config.getCitta());
            txtCap.setText(config.getCap());
            txtProvincia.setText(config.getProvincia());
            txtPiva.setText(config.getPiva());
            txtCodiceFiscale.setText(config.getCodiceFiscale());
            txtTelefono.setText(config.getTelefono());
            txtEmail.setText(config.getEmail());
        });
    }

    private void loadDatabaseConfig() {
        Properties config = databaseService.loadConfig();
        txtDbHost.setText(config.getProperty("db.host", "localhost"));
        txtDbPort.setText(config.getProperty("db.port", "3306"));
        txtDbName.setText(config.getProperty("db.name", "gestione_ingrosso"));
        txtDbUser.setText(config.getProperty("db.user", "root"));
        txtDbPassword.setText(config.getProperty("db.password", ""));
    }

    private void setupUtentiTable() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCognome.setCellValueFactory(new PropertyValueFactory<>("cognome"));

        colRuolo.setCellValueFactory(cellData -> {
            Ruolo ruolo = cellData.getValue().getRuolo();
            return new SimpleStringProperty(ruolo != null ? ruolo.getDescrizione() : "");
        });

        colAttivo.setCellValueFactory(new PropertyValueFactory<>("attivo"));
        colAttivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item ? "Si" : "No");
                }
            }
        });

        colAzioni.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifica");
            private final Button btnReset = new Button("Reset Password");
            private final HBox container = new HBox(4, btnEdit, btnReset);

            {
                btnEdit.getStyleClass().add("button-small");
                btnReset.getStyleClass().addAll("button-small", "button-outline");

                btnEdit.setOnAction(e -> {
                    Utente utente = getTableView().getItems().get(getIndex());
                    modificaUtente(utente);
                });

                btnReset.setOnAction(e -> {
                    Utente utente = getTableView().getItems().get(getIndex());
                    resetPassword(utente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadUtenti() {
        List<Utente> utenti = authService.getAllUtenti();
        tblUtenti.getItems().setAll(utenti);
    }

    @FXML
    public void salvaAzienda() {
        ConfigAzienda config = authService.getConfigAzienda().orElse(new ConfigAzienda());

        config.setNome(txtNomeAzienda.getText().trim());
        config.setIndirizzo(txtIndirizzo.getText().trim());
        config.setCitta(txtCitta.getText().trim());
        config.setCap(txtCap.getText().trim());
        config.setProvincia(txtProvincia.getText().trim());
        config.setPiva(txtPiva.getText().trim());
        config.setCodiceFiscale(txtCodiceFiscale.getText().trim());
        config.setTelefono(txtTelefono.getText().trim());
        config.setEmail(txtEmail.getText().trim());

        if (authService.saveConfigAzienda(config)) {
            AlertUtil.showInfo("Salvato", "Dati azienda salvati correttamente");
        } else {
            AlertUtil.showError("Errore", "Impossibile salvare i dati azienda");
        }
    }

    @FXML
    public void testConnessione() {
        Properties config = new Properties();
        config.setProperty("db.host", txtDbHost.getText().trim());
        config.setProperty("db.port", txtDbPort.getText().trim());
        config.setProperty("db.name", txtDbName.getText().trim());
        config.setProperty("db.user", txtDbUser.getText().trim());
        config.setProperty("db.password", txtDbPassword.getText());

        if (databaseService.testConnection(config)) {
            AlertUtil.showInfo("Connessione OK", "La connessione al database e' riuscita");
        } else {
            AlertUtil.showError("Errore connessione", "Impossibile connettersi al database. Verificare i parametri.");
        }
    }

    @FXML
    public void salvaDatabase() {
        Properties config = new Properties();
        config.setProperty("db.host", txtDbHost.getText().trim());
        config.setProperty("db.port", txtDbPort.getText().trim());
        config.setProperty("db.name", txtDbName.getText().trim());
        config.setProperty("db.user", txtDbUser.getText().trim());
        config.setProperty("db.password", txtDbPassword.getText());

        if (databaseService.saveConfig(config)) {
            AlertUtil.showInfo("Salvato", "Configurazione database salvata. Riavviare l'applicazione per applicare le modifiche.");
        } else {
            AlertUtil.showError("Errore", "Impossibile salvare la configurazione");
        }
    }

    @FXML
    public void nuovoUtente() {
        showUtenteDialog(null);
    }

    private void modificaUtente(Utente utente) {
        showUtenteDialog(utente);
    }

    private void showUtenteDialog(Utente utente) {
        Dialog<Utente> dialog = new Dialog<>();
        dialog.setTitle(utente == null ? "Nuovo Utente" : "Modifica Utente");

        // Create form
        TextField txtUsername = new TextField();
        TextField txtNome = new TextField();
        TextField txtCognome = new TextField();
        PasswordField txtPassword = new PasswordField();
        ComboBox<Ruolo> cmbRuolo = new ComboBox<>();
        CheckBox chkAttivo = new CheckBox("Attivo");

        cmbRuolo.getItems().addAll(Ruolo.values());
        cmbRuolo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Ruolo r) {
                return r != null ? r.getDescrizione() : "";
            }

            @Override
            public Ruolo fromString(String s) {
                return null;
            }
        });

        if (utente != null) {
            txtUsername.setText(utente.getUsername());
            txtUsername.setDisable(true);
            txtNome.setText(utente.getNome());
            txtCognome.setText(utente.getCognome());
            cmbRuolo.setValue(utente.getRuolo());
            chkAttivo.setSelected(utente.isAttivo());
        } else {
            cmbRuolo.setValue(Ruolo.OPERATORE);
            chkAttivo.setSelected(true);
        }

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Nome:"), 0, 1);
        grid.add(txtNome, 1, 1);
        grid.add(new Label("Cognome:"), 0, 2);
        grid.add(txtCognome, 1, 2);
        if (utente == null) {
            grid.add(new Label("Password:"), 0, 3);
            grid.add(txtPassword, 1, 3);
        }
        grid.add(new Label("Ruolo:"), 0, 4);
        grid.add(cmbRuolo, 1, 4);
        grid.add(chkAttivo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Utente result = utente != null ? utente : new Utente();
                result.setUsername(txtUsername.getText().trim());
                result.setNome(txtNome.getText().trim());
                result.setCognome(txtCognome.getText().trim());
                result.setRuolo(cmbRuolo.getValue());
                result.setAttivo(chkAttivo.isSelected());

                if (utente == null && !txtPassword.getText().isEmpty()) {
                    result.setPasswordHash(authService.hashPassword(txtPassword.getText()));
                }

                return result;
            }
            return null;
        });

        Optional<Utente> result = dialog.showAndWait();
        result.ifPresent(u -> {
            if (authService.saveUtente(u)) {
                AlertUtil.showInfo("Salvato", "Utente salvato correttamente");
                loadUtenti();
            } else {
                AlertUtil.showError("Errore", "Impossibile salvare l'utente");
            }
        });
    }

    private void resetPassword(Utente utente) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Inserisci la nuova password per " + utente.getUsername());
        dialog.setContentText("Nuova password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            if (!password.isEmpty()) {
                if (authService.updatePassword(utente.getId(), password)) {
                    AlertUtil.showInfo("Password aggiornata", "La password e' stata aggiornata correttamente");
                } else {
                    AlertUtil.showError("Errore", "Impossibile aggiornare la password");
                }
            }
        });
    }
}
