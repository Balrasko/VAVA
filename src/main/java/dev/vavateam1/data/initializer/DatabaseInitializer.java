package dev.vavateam1.data.initializer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.vavateam1.data.config.DBConfig;
import dev.vavateam1.data.connection.ConnectionFactory;

public class DatabaseInitializer {

    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());

    public static void initialize() {

        ConnectionFactory connectionFactory = new ConnectionFactory();

        try (Connection conn = connectionFactory.getConnectionToPostgresDB()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE " + DBConfig.getDatabase());
            logger.info("Database created");
        } catch (SQLException e) {
            logger.log(Level.INFO, "Database already exists", e.getMessage());
        }

        try (Connection conn = connectionFactory.getConnection()) {

            InputStream is = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql");
            if (is == null)
                throw new RuntimeException("schema.sql not found!");

            try (Scanner scanner = new Scanner(is).useDelimiter(";")) {
                Statement stmt = conn.createStatement();

                while (scanner.hasNext()) {
                    String sql = scanner.next().trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
                logger.log(Level.INFO, "Database schema initialized");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database schema initialization failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initialize();
    }
}
