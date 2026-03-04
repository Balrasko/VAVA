package dev.vavateam1.data.connection;

import dev.vavateam1.data.config.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found!", e);
        }

        return DriverManager.getConnection(DBConfig.getJdbcUrl(), DBConfig.getUser(), DBConfig.getPassword());
    }

    public static Connection getConnectionToPostgresDB() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found!", e);
        }

        String url = "jdbc:postgresql://" + DBConfig.getHost() + ":" + DBConfig.getPort() + "/postgres";
        return DriverManager.getConnection(url, DBConfig.getUser(), DBConfig.getPassword());
    }
}