package com.ingrosso.controller;

import com.ingrosso.model.Utente;
import com.ingrosso.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private StackPane contentArea;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private VBox sidebar;

    @FXML private Button btnDashboard;
    @FXML private Button btnProdotti;
    @FXML private Button btnMagazzino;
    @FXML private Button btnMovimenti;
    @FXML private Button btnDdt;
    @FXML private Button btnListini;
    @FXML private Button btnReport;
    @FXML private Button btnImpostazioni;

    private Button currentSelectedButton;
    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        // Set user info
        Utente currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getNomeCompleto());
            lblUserRole.setText(currentUser.getRuolo().getDescrizione());
        }

        // Load dashboard as default view
        navigateToDashboard();
    }

    @FXML
    public void navigateToDashboard() {
        loadContent("/fxml/dashboard.fxml");
        setSelectedButton(btnDashboard);
    }

    @FXML
    public void navigateToProdotti() {
        loadContent("/fxml/prodotti.fxml");
        setSelectedButton(btnProdotti);
    }

    @FXML
    public void navigateToMagazzino() {
        loadContent("/fxml/magazzino.fxml");
        setSelectedButton(btnMagazzino);
    }

    @FXML
    public void navigateToMovimenti() {
        loadContent("/fxml/movimenti.fxml");
        setSelectedButton(btnMovimenti);
    }

    @FXML
    public void navigateToDdt() {
        loadContent("/fxml/ddt-lista.fxml");
        setSelectedButton(btnDdt);
    }

    @FXML
    public void navigateToListini() {
        loadContent("/fxml/listini.fxml");
        setSelectedButton(btnListini);
    }

    @FXML
    public void navigateToReport() {
        loadContent("/fxml/report.fxml");
        setSelectedButton(btnReport);
    }

    @FXML
    public void navigateToImpostazioni() {
        loadContent("/fxml/impostazioni.fxml");
        setSelectedButton(btnImpostazioni);
    }

    @FXML
    public void handleLogout() {
        authService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root, 400, 500);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Gestione Ingrosso - Login");
            stage.setMinWidth(400);
            stage.setMinHeight(500);
            stage.setWidth(400);
            stage.setHeight(500);
            stage.centerOnScreen();

        } catch (IOException e) {
            logger.error("Error loading login window: {}", e.getMessage());
        }
    }

    @FXML
    public void toggleTheme() {
        Scene scene = contentArea.getScene();
        String darkTheme = getClass().getResource("/css/dark-theme.css").toExternalForm();

        if (scene.getStylesheets().contains(darkTheme)) {
            scene.getStylesheets().remove(darkTheme);
        } else {
            scene.getStylesheets().add(darkTheme);
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            logger.error("Error loading content {}: {}", fxmlPath, e.getMessage());
            Label errorLabel = new Label("Errore nel caricamento della pagina");
            errorLabel.getStyleClass().add("error-label");
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    private void setSelectedButton(Button button) {
        if (currentSelectedButton != null) {
            currentSelectedButton.getStyleClass().remove("selected");
        }
        if (button != null) {
            button.getStyleClass().add("selected");
            currentSelectedButton = button;
        }
    }

    public void refreshCurrentView() {
        if (currentSelectedButton == btnDashboard) navigateToDashboard();
        else if (currentSelectedButton == btnProdotti) navigateToProdotti();
        else if (currentSelectedButton == btnMagazzino) navigateToMagazzino();
        else if (currentSelectedButton == btnMovimenti) navigateToMovimenti();
        else if (currentSelectedButton == btnDdt) navigateToDdt();
        else if (currentSelectedButton == btnListini) navigateToListini();
        else if (currentSelectedButton == btnReport) navigateToReport();
        else if (currentSelectedButton == btnImpostazioni) navigateToImpostazioni();
    }
}
