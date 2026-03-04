package dev.vavateam1.data.config;

public class DBConfig {

    public static String getHost() {
        return System.getenv("RESTAURANT_DB_HOST");
    }

    public static String getPort() {
        return System.getenv("RESTAURANT_DB_PORT");
    }

    public static String getDatabase() {
        return System.getenv("RESTAURANT_DB_NAME");
    }

    public static String getUser() {
        return System.getenv("RESTAURANT_DB_USER");
    }

    public static String getPassword() {
        return System.getenv("RESTAURANT_DB_PASSWORD");
    }

    public static String getJdbcUrl() {
        return "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDatabase();
    }
}