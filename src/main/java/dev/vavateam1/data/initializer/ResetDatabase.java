/*THIS FILE IS FOR DEVELOPMENT ONLY*/

package dev.vavateam1.data.initializer;

import dev.vavateam1.data.connection.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ResetDatabase {

    private static final Logger logger = Logger.getLogger(ResetDatabase.class.getName());

    public static void main(String[] args) {
        try (Connection conn = ConnectionFactory.getConnection()){

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS roles CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS user_sessions CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS users CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS tables CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS locations CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS menu_items CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS categories CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS menu_item_ingredients CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS inventory_ingredients CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS order_items CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS payments CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS payment_methods CASCADE");

            System.out.println("All tables dropped successfully.");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database reset failed", e);
        }
    }
}