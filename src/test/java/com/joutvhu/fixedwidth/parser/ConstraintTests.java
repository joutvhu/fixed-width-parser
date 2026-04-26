package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import com.joutvhu.fixedwidth.parser.exception.RegexMismatchException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for @FixedRegex and @FixedOption constraints.
 * Covers: valid/invalid regex match, valid/invalid option, contains=false,
 * custom error messages, and export-time validation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConstraintTests {

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
    public static class RegexModel {
        @FixedRegex(regex = "^[A-Z]{3}$")
        @FixedField(length = 3)
        private String code;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegexCustomMessageModel {
        @FixedRegex(regex = "^[0-9]+$", message = "Must be digits", nativeMessage = true)
        @FixedField(length = 5)
        private String digits;
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
        // contains=false means value must NOT be in the list
        @FixedOption(options = {"BAD", "ERR"}, contains = false)
        @FixedField(length = 3)
        private String status;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionCustomMessageModel {
        @FixedOption(options = {"Y", "N"}, message = "Only Y or N allowed", nativeMessage = true)
        @FixedField(length = 1)
        private String flag;
    }

    // -------------------------------------------------------------------------
    // @FixedRegex — parse valid
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"ABC", "XYZ", "FOO"})
    void parseRegex_validValues(String value) {
        assertDoesNotThrow(() -> parser.parse(RegexModel.class, value));
    }

    // -------------------------------------------------------------------------
    // @FixedRegex — parse invalid
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"abc", "AB1"})
    void parseRegex_invalidValues_throw(String value) {
        assertThrows(RegexMismatchException.class,
                () -> parser.parse(RegexModel.class, value));
    }

    @Test
    void parseRegex_valueLongerThanField_truncatedBeforeValidation() {
        // "ABCD" is truncated to "ABC" (field length=3) before regex check → passes
        assertDoesNotThrow(() -> parser.parse(RegexModel.class, "ABCD"));
    }

    @Test
    void parseRegex_customNativeMessage() {
        RegexMismatchException ex = assertThrows(RegexMismatchException.class,
                () -> parser.parse(RegexCustomMessageModel.class, "abc12"));
        assertEquals("Must be digits", ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // @FixedRegex — export
    // -------------------------------------------------------------------------

    @Test
    void exportRegex_validValue_succeeds() {
        assertDoesNotThrow(() -> parser.export(new RegexModel("ABC")));
    }

    @Test
    void exportRegex_invalidValue_throws() {
        assertThrows(RegexMismatchException.class,
                () -> parser.export(new RegexModel("abc")));
    }

    // -------------------------------------------------------------------------
    // @FixedOption — parse valid
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"RED", "GRN", "BLU"})
    void parseOption_validValues(String value) {
        assertEquals(value, parser.parse(OptionModel.class, value).getColor());
    }

    // -------------------------------------------------------------------------
    // @FixedOption — parse invalid
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"WHT", "BLK"})
    void parseOption_invalidValues_throw(String value) {
        assertThrows(FixedValidationException.class,
                () -> parser.parse(OptionModel.class, value));
    }

    @Test
    void parseOption_customNativeMessage() {
        FixedValidationException ex = assertThrows(FixedValidationException.class,
                () -> parser.parse(OptionCustomMessageModel.class, "X"));
        assertEquals("Only Y or N allowed", ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // @FixedOption contains=false
    // -------------------------------------------------------------------------

    @Test
    void parseExcludeOption_allowedValue_succeeds() {
        // "OK " trimmed to "OK" — not in {"BAD","ERR"} → valid
        ExcludeOptionModel m = parser.parse(ExcludeOptionModel.class, "OK ");
        assertNotNull(m);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BAD", "ERR"})
    void parseExcludeOption_forbiddenValues_throw(String value) {
        assertThrows(FixedValidationException.class,
                () -> parser.parse(ExcludeOptionModel.class, value));
    }

    // -------------------------------------------------------------------------
    // @FixedOption — export
    // -------------------------------------------------------------------------

    @Test
    void exportOption_validValue_succeeds() {
        assertDoesNotThrow(() -> parser.export(new OptionModel("RED")));
    }

    @Test
    void exportOption_invalidValue_throws() {
        assertThrows(FixedValidationException.class,
                () -> parser.export(new OptionModel("WHT")));
    }
}
