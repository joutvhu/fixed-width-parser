package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.exception.TypeConversionException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for date/time field parsing and exporting.
 * Covers: LocalDate, LocalTime, LocalDateTime, null handling, invalid format rejection.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DateFieldTests {

    private FixedParser parser;

    @BeforeAll
    void beforeAll() {
        parser = FixedParser.parser();
    }

    // -------------------------------------------------------------------------
    // Models — public static required for FixedHelper.newInstanceOf()
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalDateModel {
        @FixedFormat(format = "yyyy-MM-dd")
        @FixedField(length = 10)
        private LocalDate date;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalTimeModel {
        @FixedFormat(format = "HH:mm:ss")
        @FixedField(length = 8)
        private LocalTime time;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalDateTimeModel {
        @FixedFormat(format = "yyyy-MM-dd HH:mm:ss")
        @FixedField(length = 19)
        private LocalDateTime dateTime;
    }

    // -------------------------------------------------------------------------
    // LocalDate
    // -------------------------------------------------------------------------

    @Test
    void parseLocalDate() {
        LocalDateModel m = parser.parse(LocalDateModel.class, "2024-03-15");
        assertEquals(LocalDate.of(2024, 3, 15), m.getDate());
    }

    @Test
    void exportLocalDate() {
        assertEquals("2024-03-15", parser.export(new LocalDateModel(LocalDate.of(2024, 3, 15))));
    }

    @Test
    void parseLocalDate_invalidFormat_throws() {
        // "15/03/2024" does not match "yyyy-MM-dd"
        assertThrows(Exception.class,
                () -> parser.parse(LocalDateModel.class, "15/03/2024"));
    }

    @Test
    void parseLocalDate_blank_returnsNull() {
        // When the entire record is blank, parse() returns null
        assertNull(parser.parse(LocalDateModel.class, "          "));
    }

    // -------------------------------------------------------------------------
    // LocalTime
    // -------------------------------------------------------------------------

    @Test
    void parseLocalTime() {
        LocalTimeModel m = parser.parse(LocalTimeModel.class, "14:30:00");
        assertEquals(LocalTime.of(14, 30, 0), m.getTime());
    }

    @Test
    void exportLocalTime() {
        assertEquals("14:30:00", parser.export(new LocalTimeModel(LocalTime.of(14, 30, 0))));
    }

    // -------------------------------------------------------------------------
    // LocalDateTime
    // -------------------------------------------------------------------------

    @Test
    void parseLocalDateTime() {
        LocalDateTimeModel m = parser.parse(LocalDateTimeModel.class, "2024-03-15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 0), m.getDateTime());
    }

    @Test
    void exportLocalDateTime() {
        assertEquals("2024-03-15 14:30:00",
                parser.export(new LocalDateTimeModel(LocalDateTime.of(2024, 3, 15, 14, 30, 0))));
    }

    // -------------------------------------------------------------------------
    // Round-trip
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_localDate() {
        LocalDateModel original = new LocalDateModel(LocalDate.of(2000, 1, 1));
        assertEquals(original.getDate(),
                parser.parse(LocalDateModel.class, parser.export(original)).getDate());
    }

    @Test
    void roundTrip_localDateTime() {
        LocalDateTimeModel original = new LocalDateTimeModel(LocalDateTime.of(1999, 12, 31, 23, 59, 59));
        assertEquals(original.getDateTime(),
                parser.parse(LocalDateTimeModel.class, parser.export(original)).getDateTime());
    }
}
