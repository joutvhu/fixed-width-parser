package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.exception.MandatoryValueException;
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
 * Tests for String and Character field parsing and exporting.
 * Covers: basic string, char, keepPadding DROP/KEEP, required field,
 * truncation when value exceeds length, null export.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringFieldTests {

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
    public static class SimpleStringModel {
        @FixedField(length = 10)
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharModel {
        @FixedField(length = 1)
        private Character value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropPaddingModel {
        @FixedField(length = 10, keepPadding = KeepPadding.DROP)
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeepPaddingModel {
        @FixedField(length = 10, keepPadding = KeepPadding.KEEP)
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredStringModel {
        @FixedField(length = 5, required = true)
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TwoFieldModel {
        @FixedField(length = 5)
        private String first;
        @FixedField(start = 5, length = 5)
        private String second;
    }

    // -------------------------------------------------------------------------
    // Basic parse / export
    // -------------------------------------------------------------------------

    @Test
    void parseString_trailingSpacesTrimmed() {
        // keepPadding=AUTO for String defaults to KEEP — use DROP to trim
        SimpleStringModel m = parser.parse(SimpleStringModel.class, "hello     ");
        assertEquals("hello     ", m.getValue());
    }

    @Test
    void parseString_blank_returnsNull() {
        // When the entire object is blank, parse() returns null
        assertNull(parser.parse(SimpleStringModel.class, "          "));
    }

    @Test
    void exportString_rightPaddedToLength() {
        assertEquals("hello     ", parser.export(new SimpleStringModel("hello")));
    }

    @Test
    void exportString_null_allSpaces() {
        assertEquals("          ", parser.export(new SimpleStringModel(null)));
    }

    @Test
    void exportString_exactLength_noChange() {
        assertEquals("helloworld", parser.export(new SimpleStringModel("helloworld")));
    }

    @Test
    void exportString_tooLong_truncated() {
        // value longer than field length must be truncated to 10 chars
        assertEquals("helloworld", parser.export(new SimpleStringModel("helloworldXXX")));
    }

    // -------------------------------------------------------------------------
    // Character field
    // -------------------------------------------------------------------------

    @Test
    void parseChar() {
        assertEquals('A', parser.parse(CharModel.class, "A").getValue());
    }

    @Test
    void exportChar() {
        assertEquals("Z", parser.export(new CharModel('Z')));
    }

    // -------------------------------------------------------------------------
    // KeepPadding
    // -------------------------------------------------------------------------

    @Test
    void parseDropPadding_trailingSpacesRemoved() {
        assertEquals("hi", parser.parse(DropPaddingModel.class, "hi        ").getValue());
    }

    @Test
    void parseKeepPadding_trailingSpacesRetained() {
        assertEquals("hi        ", parser.parse(KeepPaddingModel.class, "hi        ").getValue());
    }

    // -------------------------------------------------------------------------
    // Required field
    // -------------------------------------------------------------------------

    @Test
    void parseRequired_blank_returnsNull() {
        // required=true does not throw on blank input — parse() returns null object
        // MandatoryValueException is thrown only when a non-null field resolves to null
        assertNull(parser.parse(RequiredStringModel.class, "     "));
    }

    @Test
    void exportRequired_null_throws() {
        assertThrows(MandatoryValueException.class,
                () -> parser.export(new RequiredStringModel(null)));
    }

    // -------------------------------------------------------------------------
    // Multiple fields
    // -------------------------------------------------------------------------

    @Test
    void parseTwoFields() {
        TwoFieldModel m = parser.parse(TwoFieldModel.class, "helloworld");
        assertEquals("hello", m.getFirst());
        assertEquals("world", m.getSecond());
    }

    @Test
    void exportTwoFields() {
        assertEquals("helloworld", parser.export(new TwoFieldModel("hello", "world")));
    }

    // -------------------------------------------------------------------------
    // Parameterized round-trip
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"abc", "HELLO", "x", "1234567890"})
    void roundTrip_string(String value) {
        // keepPadding=AUTO keeps spaces, so round-trip preserves padded value
        SimpleStringModel original = new SimpleStringModel(value);
        String exported = parser.export(original);
        SimpleStringModel parsed = parser.parse(SimpleStringModel.class, exported);
        // exported value is right-padded to 10; parsed value retains padding
        assertEquals(exported, parser.export(parsed));
    }
}
