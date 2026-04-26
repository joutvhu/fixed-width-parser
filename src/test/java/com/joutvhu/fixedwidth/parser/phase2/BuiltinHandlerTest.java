package com.joutvhu.fixedwidth.parser.phase2;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import com.joutvhu.fixedwidth.parser.exception.RegexMismatchException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2 — Built-in handlers
 *
 * Kiểm tra các built-in handler (migrate từ validator cũ) hoạt động đúng.
 * Tất cả test này sẽ FAIL cho đến khi Phase 2 được implement.
 */
class BuiltinHandlerTest {

    // -------------------------------------------------------------------------
    // Models
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegexModel {
        @FixedRegex(regex = "^[A-Z]{3}$")
        @FixedField(length = 3)
        private String code;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionModel {
        @FixedOption(options = {"RED", "GRN", "BLU"})
        @FixedField(length = 3)
        private String color;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExcludeOptionModel {
        @FixedOption(options = {"BAD", "ERR"}, contains = false)
        @FixedField(length = 3)
        private String status;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BooleanYNModel {
        @FixedFormat(format = "Y|N")
        @FixedField(length = 1)
        private Boolean flag;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateModel {
        @FixedFormat(format = "yyyy-MM-dd")
        @FixedField(length = 10)
        private LocalDate date;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateTimeModel {
        @FixedFormat(format = "yyyy-MM-dd HH:mm:ss")
        @FixedField(length = 19)
        private LocalDateTime dateTime;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeModel {
        @FixedFormat(format = "HH:mm:ss")
        @FixedField(length = 8)
        private LocalTime time;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NumberModel {
        @FixedField(length = 5)
        private Integer intVal;

        @FixedField(start = 5, length = 8)
        private Double doubleVal;
    }

    // -------------------------------------------------------------------------
    // RegexHandler
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"ABC", "XYZ", "FOO"})
    void regexHandler_validValues_pass(String value) {
        assertDoesNotThrow(() -> FixedParser.parser().parse(RegexModel.class, value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "AB1"})
    void regexHandler_invalidValues_throwRegexMismatch(String value) {
        assertThrows(RegexMismatchException.class,
                () -> FixedParser.parser().parse(RegexModel.class, value));
    }

    @Test
    void regexHandler_validatesOnWrite() {
        assertDoesNotThrow(() -> FixedParser.parser().export(new RegexModel("ABC")));
        assertThrows(RegexMismatchException.class,
                () -> FixedParser.parser().export(new RegexModel("abc")));
    }

    // -------------------------------------------------------------------------
    // OptionHandler
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"RED", "GRN", "BLU"})
    void optionHandler_validValues_pass(String value) {
        OptionModel model = FixedParser.parser().parse(OptionModel.class, value);
        assertEquals(value, model.getColor());
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHT", "BLK"})
    void optionHandler_invalidValues_throwValidationException(String value) {
        assertThrows(FixedValidationException.class,
                () -> FixedParser.parser().parse(OptionModel.class, value));
    }

    @Test
    void optionHandler_containsFalse_forbiddenValueThrows() {
        assertThrows(FixedValidationException.class,
                () -> FixedParser.parser().parse(ExcludeOptionModel.class, "BAD"));
    }

    @Test
    void optionHandler_containsFalse_allowedValuePasses() {
        assertDoesNotThrow(() -> FixedParser.parser().parse(ExcludeOptionModel.class, "OK "));
    }

    @Test
    void optionHandler_validatesOnWrite() {
        assertDoesNotThrow(() -> FixedParser.parser().export(new OptionModel("RED")));
        assertThrows(FixedValidationException.class,
                () -> FixedParser.parser().export(new OptionModel("WHT")));
    }

    // -------------------------------------------------------------------------
    // BooleanHandler — Y|N format
    // -------------------------------------------------------------------------

    @Test
    void booleanHandler_parseY_returnsTrue() {
        assertEquals(true, FixedParser.parser().parse(BooleanYNModel.class, "Y").getFlag());
    }

    @Test
    void booleanHandler_parseN_returnsFalse() {
        assertEquals(false, FixedParser.parser().parse(BooleanYNModel.class, "N").getFlag());
    }

    @Test
    void booleanHandler_invalidValue_throwsValidationException() {
        assertThrows(FixedValidationException.class,
                () -> FixedParser.parser().parse(BooleanYNModel.class, "X"));
    }

    @Test
    void booleanHandler_exportTrue_returnsY() {
        assertEquals("Y", FixedParser.parser().export(new BooleanYNModel(true)));
    }

    @Test
    void booleanHandler_exportFalse_returnsN() {
        assertEquals("N", FixedParser.parser().export(new BooleanYNModel(false)));
    }

    // -------------------------------------------------------------------------
    // DateHandler
    // -------------------------------------------------------------------------

    @Test
    void dateHandler_parseValidDate() {
        DateModel model = FixedParser.parser().parse(DateModel.class, "2024-01-15");
        assertEquals(LocalDate.of(2024, 1, 15), model.getDate());
    }

    @Test
    void dateHandler_parseInvalidFormat_throws() {
        assertThrows(Exception.class,
                () -> FixedParser.parser().parse(DateModel.class, "15/01/2024"));
    }

    @Test
    void dateHandler_exportDate() {
        String result = FixedParser.parser().export(new DateModel(LocalDate.of(2024, 1, 15)));
        assertEquals("2024-01-15", result);
    }

    @Test
    void dateHandler_parseDateTime() {
        DateTimeModel model = FixedParser.parser()
                .parse(DateTimeModel.class, "2024-01-15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 30, 0), model.getDateTime());
    }

    @Test
    void dateHandler_parseTime() {
        TimeModel model = FixedParser.parser().parse(TimeModel.class, "14:30:00");
        assertEquals(LocalTime.of(14, 30, 0), model.getTime());
    }

    // -------------------------------------------------------------------------
    // NumberHandler
    // -------------------------------------------------------------------------

    @Test
    void numberHandler_parseInteger() {
        NumberModel model = FixedParser.parser().parse(NumberModel.class, "00042000001.5 ");
        assertEquals(42, model.getIntVal());
    }

    @Test
    void numberHandler_parseDouble() {
        NumberModel model = FixedParser.parser().parse(NumberModel.class, "000420000001.5");
        assertEquals(1.5, model.getDoubleVal(), 0.001);
    }

    @Test
    void numberHandler_invalidInteger_throws() {
        assertThrows(Exception.class,
                () -> FixedParser.parser().parse(NumberModel.class, "abc  000001.5 "));
    }

    // -------------------------------------------------------------------------
    // Round-trip: parse → export → parse
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_date() {
        DateModel original = new DateModel(LocalDate.of(2024, 6, 15));
        String exported = FixedParser.parser().export(original);
        DateModel parsed = FixedParser.parser().parse(DateModel.class, exported);
        assertEquals(original.getDate(), parsed.getDate());
    }

    @Test
    void roundTrip_boolean() {
        BooleanYNModel original = new BooleanYNModel(true);
        String exported = FixedParser.parser().export(original);
        BooleanYNModel parsed = FixedParser.parser().parse(BooleanYNModel.class, exported);
        assertEquals(original.getFlag(), parsed.getFlag());
    }
}
