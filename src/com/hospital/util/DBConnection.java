package com.hospital.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Singleton database connection helper.
 * Manages JDBC connection pooling, reads properties from config.properties,
 * and handles database/schema auto-generation.
 */
public class DBConnection {
    private static final String CONFIG_FILE = "config.properties";
    
    private static String host;
    private static String port;
    private static String dbName;
    private static String username;
    private static String password;
    private static String dbMode;
    
    static {
        loadConfig();
    }

    /**
     * Loads database configuration from the properties file.
     */
    public static void loadConfig() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        
        // Attempt to load properties
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                host = props.getProperty("db.host", "localhost");
                port = props.getProperty("db.port", "3306");
                dbName = props.getProperty("db.name", "hospital_db");
                username = props.getProperty("db.username", "root");
                password = props.getProperty("db.password", "root");
                dbMode = props.getProperty("db.mode", "mysql");
            } catch (IOException e) {
                setDefaults();
            }
        } else {
            setDefaults();
            try {
                saveConfig(host, port, dbName, username, password, dbMode);
            } catch (IOException ignored) {}
        }
    }

    private static void setDefaults() {
        host = "localhost";
        port = "3306";
        dbName = "hospital_db";
        username = "root";
        password = "root";
        dbMode = "mysql";
    }

    /**
     * Saves database credentials to config.properties and reloads them.
     */
    public static void saveConfig(String newHost, String newPort, String newDbName, String newUsername, String newPassword, String newDbMode) throws IOException {
        Properties props = new Properties();
        props.setProperty("db.host", newHost);
        props.setProperty("db.port", newPort);
        props.setProperty("db.name", newDbName);
        props.setProperty("db.username", newUsername);
        props.setProperty("db.password", newPassword);
        props.setProperty("db.mode", newDbMode);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Database Configurations");
        }
        
        host = newHost;
        port = newPort;
        dbName = newDbName;
        username = newUsername;
        password = newPassword;
        dbMode = newDbMode;
    }

    public static void saveConfig(String newHost, String newPort, String newDbName, String newUsername, String newPassword) throws IOException {
        saveConfig(newHost, newPort, newDbName, newUsername, newPassword, dbMode);
    }

    /**
     * Tests connectivity to the database and performs database/table initialization.
     * Throws SQLException if connection fails.
     */
    public static void testAndInitialize() throws SQLException {
        if ("memory".equalsIgnoreCase(dbMode)) {
            return; // Skip DB checks in offline simulation mode
        }
        // Load the driver explicitly to avoid compatibility issues in some JVM configurations
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath.", e);
        }

        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try {
            // 1. Establish server connection to verify credentials and ensure DB exists
            try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            }

            // 2. Establish connection to the created database and execute tables creation
            try (Connection conn = DriverManager.getConnection(dbUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                
                // Create patients table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS patients (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  name VARCHAR(100) NOT NULL," +
                    "  age INT NOT NULL," +
                    "  gender VARCHAR(10) NOT NULL," +
                    "  phone VARCHAR(15) UNIQUE NOT NULL," +
                    "  email VARCHAR(100) NOT NULL" +
                    ")"
                );

                // Create appointments table
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS appointments (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  patient_id INT NOT NULL," +
                    "  doctor_name VARCHAR(100) NOT NULL," +
                    "  appointment_date DATE NOT NULL," +
                    "  appointment_time VARCHAR(20) NOT NULL," +
                    "  status VARCHAR(20) NOT NULL DEFAULT 'Scheduled'," +
                    "  FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE" +
                    ")"
                );
            }
        } catch (SQLException e) {
            if ("28000".equals(e.getSQLState()) || e.getErrorCode() == 1045) {
                throw new SQLException(
                    "MySQL Authentication Failed!\n" +
                    "Access Denied for user '" + username + "'@'" + host + "' (using password: " + (!password.isEmpty() ? "YES" : "NO") + ").\n" +
                    "Please verify and adjust your database password in 'config.properties' or the database setup window.", 
                    e.getSQLState(), 
                    e.getErrorCode(), 
                    e
                );
            }
            throw e;
        }
    }

    /**
     * Returns a Connection object to the configured database.
     */
    public static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            return DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException e) {
            if ("28000".equals(e.getSQLState()) || e.getErrorCode() == 1045) {
                throw new SQLException(
                    "MySQL Authentication Failed!\n" +
                    "Access Denied for user '" + username + "'@'" + host + "' (using password: " + (!password.isEmpty() ? "YES" : "NO") + ").\n" +
                    "Please verify your credentials in 'config.properties'.", 
                    e.getSQLState(), 
                    e.getErrorCode(), 
                    e
                );
            }
            throw e;
        }
    }

    // Getters for configuration data
    public static String getHost() { return host; }
    public static String getPort() { return port; }
    public static String getDbName() { return dbName; }
    public static String getUsername() { return username; }
    public static String getPassword() { return password; }
    public static String getDbMode() { return dbMode; }
    public static void setDbMode(String mode) {
        dbMode = mode;
        try {
            saveConfig(host, port, dbName, username, password, dbMode);
        } catch (IOException ignored) {}
    }
}
