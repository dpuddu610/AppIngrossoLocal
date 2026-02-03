package com.ingrosso.service;

import com.ingrosso.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static DatabaseService instance;

    private boolean initialized = false;

    private DatabaseService() {}

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public boolean initialize() {
        Properties config = DatabaseUtil.loadConfig();

        if (config.isEmpty()) {
            logger.warn("No database configuration found");
            return false;
        }

        String host = config.getProperty("db.host", "localhost");
        int port = Integer.parseInt(config.getProperty("db.port", "3306"));
        String database = config.getProperty("db.database", "gestione_ingrosso");
        String username = config.getProperty("db.username", "root");
        String password = config.getProperty("db.password", "");

        return initialize(host, port, database, username, password);
    }

    public boolean initialize(String host, int port, String database, String username, String password) {
        try {
            // Test connection first
            if (!DatabaseUtil.testConnection(host, port, database, username, password)) {
                logger.error("Failed to connect to database");
                return false;
            }

            // Initialize connection pool
            DatabaseUtil.initialize(host, port, database, username, password);

            // Save configuration
            DatabaseUtil.saveConfig(host, port, database, username, password);

            // Initialize schema if needed
            initializeSchema();

            initialized = true;
            logger.info("Database service initialized successfully");
            return true;

        } catch (Exception e) {
            logger.error("Failed to initialize database service: {}", e.getMessage());
            return false;
        }
    }

    public boolean testConnection(String host, int port, String database, String username, String password) {
        return DatabaseUtil.testConnection(host, port, database, username, password);
    }

    private void initializeSchema() {
        try (InputStream is = getClass().getResourceAsStream("/sql/schema.sql")) {
            if (is == null) {
                logger.warn("Schema file not found, skipping initialization");
                return;
            }

            String schema = new String(is.readAllBytes());
            executeSchema(schema);
            logger.info("Database schema initialized");

        } catch (IOException e) {
            logger.error("Error reading schema file: {}", e.getMessage());
        }
    }

    private void executeSchema(String schema) {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // Split by semicolon, handling multi-line statements
            String[] statements = schema.split(";");
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--") && !sql.startsWith("/*")) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        // Ignore errors for CREATE TABLE IF NOT EXISTS, etc.
                        if (!e.getMessage().contains("already exists") &&
                            !e.getMessage().contains("Duplicate")) {
                            logger.debug("Schema statement skipped: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing schema: {}", e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized && DatabaseUtil.isInitialized();
    }

    public void shutdown() {
        DatabaseUtil.close();
        initialized = false;
        logger.info("Database service shut down");
    }

    public boolean hasConfiguration() {
        Properties config = DatabaseUtil.loadConfig();
        return !config.isEmpty();
    }

    public String getHost() {
        return DatabaseUtil.getHost();
    }

    public int getPort() {
        return DatabaseUtil.getPort();
    }

    public String getDatabase() {
        return DatabaseUtil.getDatabase();
    }

    public String getUsername() {
        return DatabaseUtil.getUsername();
    }

    public String backupDatabase(String outputPath) {
        // This would require mysqldump or similar - placeholder for now
        logger.warn("Database backup not implemented");
        return null;
    }
}
