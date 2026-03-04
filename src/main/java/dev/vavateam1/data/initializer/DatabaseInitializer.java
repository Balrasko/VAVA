package dev.vavateam1.data.initializer;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.data.config.DBConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseInitializer {

    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());

    public static void initialize() {
        try (Connection conn = ConnectionFactory.getConnectionToPostgresDB()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE " + DBConfig.getDatabase());
            logger.info("Database created");
        } catch (SQLException e) {
            logger.log(Level.INFO, "Database already exists", e.getMessage());
        }

        try (Connection conn = ConnectionFactory.getConnection()) {

            InputStream is = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql");
            if (is == null) throw new RuntimeException("schema.sql not found!");

            Scanner scanner = new Scanner(is).useDelimiter(";");
            Statement stmt = conn.createStatement();

            while (scanner.hasNext()) {
                String sql = scanner.next().trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
            logger.log(Level.INFO, "Database schema initialized");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database schema initialization failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initialize();
    }
}