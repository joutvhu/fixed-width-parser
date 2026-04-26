package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for boolean field parsing and exporting.
 * Covers: default true/false tokens, custom Y|N format, length-based defaults,
 * null handling, and BooleanValidator rejection.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BooleanFieldTests {

    private FixedParser parser;

    @BeforeAll
    void beforeAll() {
        parser = FixedParser.parser();
    }

    // -------------------------------------------------------------------------
    // Models — must be public static for FixedHelper.newInstanceOf()
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YNModel {
        @FixedFormat(format = "Y|N")
        @FixedField(length = 1)
        private Boolean flag;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrueFalseModel {
        // length=5 → default tokens "TRUE"/"FALSE" (length > 4)
        @FixedField(length = 5)
        private Boolean flag;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YesNoModel {
        // length=3 → default tokens "YES"/"NO" (length > 2)
        @FixedField(length = 3)
        private Boolean flag;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TFModel {
        // length=1 → default tokens "T"/"F"
        @FixedField(length = 1)
        private Boolean flag;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NullableBoolModel {
        @FixedField(length = 3)
        private Boolean flag;   // wrapper — nullable, length > 1 so blank → null
    }

    // -------------------------------------------------------------------------
    // Parse: Y|N format
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({"Y,true", "N,false"})
    void parseYN(String input, boolean expected) {
        assertEquals(expected, parser.parse(YNModel.class, input).getFlag());
    }

    @Test
    void parseYN_invalidValue_throwsValidationException() {
        assertThrows(FixedValidationException.class,
                () -> parser.parse(YNModel.class, "X"));
    }

    // -------------------------------------------------------------------------
    // Parse: well-known tokens (no @FixedFormat)
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({"T,true", "F,false", "1,true", "0,false"})
    void parseTF(String input, boolean expected) {
        assertEquals(expected, parser.parse(TFModel.class, input).getFlag());
    }

    @Test
    void parseYesNo_true() {
        assertEquals(true, parser.parse(YesNoModel.class, "YES").getFlag());
    }

    @Test
    void parseYesNo_false() {
        assertEquals(false, parser.parse(YesNoModel.class, "NO ").getFlag());
    }

    @Test
    void parseTrueFalse_true() {
        assertEquals(true, parser.parse(TrueFalseModel.class, "TRUE ").getFlag());
    }

    // -------------------------------------------------------------------------
    // Parse: null wrapper Boolean
    // -------------------------------------------------------------------------

    @Test
    void parseBlank_nullableBoolean_returnsNullObject() {
        // When the entire record is blank, parse() returns null
        assertNull(parser.parse(NullableBoolModel.class, "   "));
    }

    // -------------------------------------------------------------------------
    // Export: Y|N format
    // -------------------------------------------------------------------------

    @Test
    void exportTrue_YN() {
        assertEquals("Y", parser.export(new YNModel(true)));
    }

    @Test
    void exportFalse_YN() {
        assertEquals("N", parser.export(new YNModel(false)));
    }

    @Test
    void exportNull_YN_returnsSpace() {
        assertEquals(" ", parser.export(new YNModel(null)));
    }

    // -------------------------------------------------------------------------
    // Export: length-based default tokens
    // -------------------------------------------------------------------------

    @Test
    void exportTrue_lengthOne_returnsT() {
        assertEquals("T", parser.export(new TFModel(true)));
    }

    @Test
    void exportFalse_lengthOne_returnsF() {
        assertEquals("F", parser.export(new TFModel(false)));
    }

    @Test
    void exportTrue_lengthThree_returnsYES() {
        assertEquals("YES", parser.export(new YesNoModel(true)));
    }

    @Test
    void exportFalse_lengthThree_returnsNO() {
        // "NO" right-padded to length 3
        assertEquals("NO ", parser.export(new YesNoModel(false)));
    }

    @Test
    void exportTrue_lengthFive_returnsTRUE() {
        assertEquals("TRUE ", parser.export(new TrueFalseModel(true)));
    }

    @Test
    void exportFalse_lengthFive_returnsFALSE() {
        assertEquals("FALSE", parser.export(new TrueFalseModel(false)));
    }

    // -------------------------------------------------------------------------
    // Round-trip
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_YN() {
        YNModel original = new YNModel(true);
        String line = parser.export(original);
        YNModel parsed = parser.parse(YNModel.class, line);
        assertEquals(original.getFlag(), parsed.getFlag());
    }
}
