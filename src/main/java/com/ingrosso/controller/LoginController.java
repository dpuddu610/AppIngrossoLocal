package com.ingrosso.controller;

import com.ingrosso.model.Utente;
import com.ingrosso.service.AuthService;
import com.ingrosso.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;
    @FXML private ProgressIndicator progressIndicator;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        // Enter key triggers login
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        txtUsername.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                txtPassword.requestFocus();
            }
        });

        // Hide error and progress
        if (lblError != null) lblError.setVisible(false);
        if (progressIndicator != null) progressIndicator.setVisible(false);

        // Focus username field
        txtUsername.requestFocus();
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Validation
        if (username.isEmpty()) {
            showError("Inserire username");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Inserire password");
            txtPassword.requestFocus();
            return;
        }

        // Show progress
        setLoading(true);
        hideError();

        // Attempt login
        Optional<Utente> utenteOpt = authService.login(username, password);

        setLoading(false);

        if (utenteOpt.isPresent()) {
            logger.info("Login successful for user: {}", username);
            openMainWindow();
        } else {
            showError("Username o password non validi");
            txtPassword.clear();
            txtPassword.requestFocus();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);

            // Load stylesheet
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Gestione Ingrosso");
            stage.setMinWidth(1024);
            stage.setMinHeight(768);
            stage.centerOnScreen();

        } catch (IOException e) {
            logger.error("Error loading main window: {}", e.getMessage());
            AlertUtil.showError("Errore", "Impossibile aprire la finestra principale");
        }
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }

    private void hideError() {
        if (lblError != null) {
            lblError.setVisible(false);
        }
    }

    private void setLoading(boolean loading) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(loading);
        }
        btnLogin.setDisable(loading);
        txtUsername.setDisable(loading);
        txtPassword.setDisable(loading);
    }
}
