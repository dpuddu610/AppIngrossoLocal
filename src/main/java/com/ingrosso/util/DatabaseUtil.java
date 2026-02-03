package com.ingrosso.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static HikariDataSource dataSource;
    private static final String CONFIG_FILE = "gestione_ingrosso.properties";

    private DatabaseUtil() {}

    public static void initialize(String host, int port, String database, String username, String password) {
        if (dataSource != null) {
            dataSource.close();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Europe/Rome&allowPublicKeyRetrieval=true",
                host, port, database));
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1200000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized for {}:{}/{}", host, port, database);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized. Call initialize() first.");
        }
        return dataSource.getConnection();
    }

    public static boolean testConnection(String host, int port, String database, String username, String password) {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Europe/Rome&allowPublicKeyRetrieval=true",
                host, port, database);
        try (Connection conn = java.sql.DriverManager.getConnection(url, username, password)) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            logger.info("Database connection pool closed");
        }
    }

    public static boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }

    public static void executeScript(String scriptPath) throws SQLException, IOException {
        String script;
        try (InputStream is = DatabaseUtil.class.getResourceAsStream(scriptPath)) {
            if (is == null) {
                throw new IOException("Script not found: " + scriptPath);
            }
            script = new String(is.readAllBytes());
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String[] statements = script.split(";");
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        logger.warn("Error executing statement: {} - {}", sql.substring(0, Math.min(50, sql.length())), e.getMessage());
                    }
                }
            }
        }
        logger.info("Script executed: {}", scriptPath);
    }

    public static Properties loadConfig() {
        Properties props = new Properties();
        Path configPath = getConfigPath();

        if (Files.exists(configPath)) {
            try (InputStream is = Files.newInputStream(configPath)) {
                props.load(is);
                logger.info("Configuration loaded from: {}", configPath);
            } catch (IOException e) {
                logger.error("Error loading configuration: {}", e.getMessage());
            }
        }
        return props;
    }

    public static void saveConfig(String host, int port, String database, String username, String password) {
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.database", database);
        props.setProperty("db.username", username);
        props.setProperty("db.password", password);

        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream os = Files.newOutputStream(configPath)) {
                props.store(os, "Gestione Ingrosso - Database Configuration");
            }
            logger.info("Configuration saved to: {}", configPath);
        } catch (IOException e) {
            logger.error("Error saving configuration: {}", e.getMessage());
        }
    }

    private static Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".gestione_ingrosso", CONFIG_FILE);
    }

    public static String getHost() {
        Properties props = loadConfig();
        return props.getProperty("db.host", "localhost");
    }

    public static int getPort() {
        Properties props = loadConfig();
        return Integer.parseInt(props.getProperty("db.port", "3306"));
    }

    public static String getDatabase() {
        Properties props = loadConfig();
        return props.getProperty("db.database", "gestione_ingrosso");
    }

    public static String getUsername() {
        Properties props = loadConfig();
        return props.getProperty("db.username", "root");
    }

    public static String getPassword() {
        Properties props = loadConfig();
        return props.getProperty("db.password", "");
    }
}
