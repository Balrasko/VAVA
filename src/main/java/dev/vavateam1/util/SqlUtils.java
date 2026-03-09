package dev.vavateam1.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SqlUtils {
    private SqlUtils() {}

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
