package dev.vavateam1.data.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DBConfig {

    private static final Map<String, String> ENV = new HashMap<>();

    static {
        try {
            Files.readAllLines(Paths.get(".env")).forEach(line -> {
                if (line.contains("=") && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    ENV.put(parts[0].trim(), parts[1].trim());
                }
            });
        } catch (IOException e) {
            System.err.println("Warning: .env file not found or could not be read. Falling back to System.getenv()");
        }
    }

    private static String getEnv(String key) {
        String value = ENV.get(key);
        if (value != null)
            return value;

        return System.getenv(key);
    }

    public static String getHost() {
        return getEnv("RESTAURANT_DB_HOST");
    }

    public static String getPort() {
        return getEnv("RESTAURANT_DB_PORT");
    }

    public static String getDatabase() {
        return getEnv("RESTAURANT_DB_NAME");
    }

    public static String getUser() {
        return getEnv("RESTAURANT_DB_USER");
    }

    public static String getPassword() {
        return getEnv("RESTAURANT_DB_PASSWORD");
    }

    public static String getJdbcUrl() {
        return "jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDatabase();
    }
}