package com.ingrosso;

import com.ingrosso.service.DatabaseService;
import com.ingrosso.util.AlertUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final String APP_TITLE = "Gestione Ingrosso";
    private static final int LOGIN_WIDTH = 400;
    private static final int LOGIN_HEIGHT = 500;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            if (!initializeDatabase()) {
                showDatabaseConfigDialog(primaryStage);
                return;
            }

            // Load login screen
            showLoginScreen(primaryStage);

        } catch (Exception e) {
            logger.error("Error starting application: {}", e.getMessage(), e);
            showFatalError("Errore di avvio", "Impossibile avviare l'applicazione: " + e.getMessage());
            Platform.exit();
        }
    }

    private boolean initializeDatabase() {
        DatabaseService dbService = DatabaseService.getInstance();

        // Check if config exists
        if (!dbService.configExists()) {
            logger.info("Database configuration not found");
            return false;
        }

        // Try to connect
        if (!dbService.initialize()) {
            logger.error("Failed to initialize database connection");
            return false;
        }

        // Initialize schema if needed
        if (!dbService.initializeSchema()) {
            logger.warn("Failed to initialize database schema");
        }

        return true;
    }

    private void showDatabaseConfigDialog(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Configurazione Database");
        alert.setHeaderText("Database non configurato");
        alert.setContentText("E' necessario configurare la connessione al database MySQL.\n\n" +
                "Assicurarsi che MySQL sia in esecuzione e che il database 'gestione_ingrosso' sia stato creato.\n\n" +
                "Vuoi procedere con la configurazione?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Show settings directly
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/impostazioni.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                primaryStage.setTitle(APP_TITLE + " - Configurazione");
                primaryStage.setScene(scene);
                primaryStage.show();

            } catch (IOException e) {
                logger.error("Error loading settings screen: {}", e.getMessage());
                showFatalError("Errore", "Impossibile caricare la schermata di configurazione");
                Platform.exit();
            }
        } else {
            Platform.exit();
        }
    }

    private void showLoginScreen(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle(APP_TITLE + " - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(LOGIN_WIDTH);
            primaryStage.setMinHeight(LOGIN_HEIGHT);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

            logger.info("Application started successfully");

        } catch (IOException e) {
            logger.error("Error loading login screen: {}", e.getMessage(), e);
            showFatalError("Errore", "Impossibile caricare la schermata di login");
            Platform.exit();
        }
    }

    private void showFatalError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        logger.info("Application stopping...");
        DatabaseService.getInstance().shutdown();
        logger.info("Application stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
