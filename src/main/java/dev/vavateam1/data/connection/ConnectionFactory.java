package dev.vavateam1.data.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import dev.vavateam1.data.config.DBConfig;

public class ConnectionFactory {
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found!", e);
        }

        return DriverManager.getConnection(DBConfig.getJdbcUrl(), DBConfig.getUser(), DBConfig.getPassword());
    }

    public Connection getConnectionToPostgresDB() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found!", e);
        }

        String url = "jdbc:postgresql://" + DBConfig.getHost() + ":" + DBConfig.getPort() + "/postgres";
        return DriverManager.getConnection(url, DBConfig.getUser(), DBConfig.getPassword());
    }
}