package dev.vavateam1.util;

import org.junit.jupiter.api.Test;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SqlUtilsTest {

    @Test
    void toLocalDateTime_nullInput_returnsNull() {
        assertNull(SqlUtils.toLocalDateTime(null));
    }

    @Test
    void toLocalDateTime_validTimestamp_convertsCorrectly() {
        LocalDateTime expected = LocalDateTime.of(2024, 4, 27, 12, 0, 0);
        Timestamp ts = Timestamp.valueOf(expected);
        assertEquals(expected, SqlUtils.toLocalDateTime(ts));
    }
}