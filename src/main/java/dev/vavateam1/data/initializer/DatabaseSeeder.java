package dev.vavateam1.data.initializer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.vavateam1.data.connection.ConnectionFactory;

public class DatabaseSeeder {

    private static final Logger logger = Logger.getLogger(DatabaseSeeder.class.getName());

    public static void seed() {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        try (Connection conn = connectionFactory.getConnection()) {
            InputStream is = DatabaseSeeder.class.getResourceAsStream("/db/seed.sql");
            if (is == null)
                throw new RuntimeException("seed.sql not found!");

            try (Scanner scanner = new Scanner(is).useDelimiter(";")) {
                Statement stmt = conn.createStatement();

                while (scanner.hasNext()) {
                    String sql = scanner.next().trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
                logger.log(Level.INFO, "Database default data seeded successfully.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database seeding failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        seed();
    }
}
