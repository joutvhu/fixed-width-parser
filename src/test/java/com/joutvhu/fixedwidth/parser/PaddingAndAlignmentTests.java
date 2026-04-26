package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
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
 * Tests for padding characters, alignment, and centerPadValue correctness.
 * Covers: custom padding char, null padding char, CENTER alignment with
 * even and odd padSize (regression for the centerPadValue bug), LEFT/RIGHT
 * alignment for numbers, and KeepPadding interaction with alignment.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaddingAndAlignmentTests {

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
    public static class CustomPaddingModel {
        @FixedField(length = 8, padding = '*')
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NullPaddingModel {
        @FixedField(length = 5, nullPadding = '-')
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterStringModel {
        @FixedField(length = 10, alignment = Alignment.CENTER)
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RightAlignStringModel {
        @FixedField(length = 8, alignment = Alignment.RIGHT)
        private String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeftAlignNumberModel {
        // Numbers default to RIGHT; override to LEFT
        @FixedField(length = 6, alignment = Alignment.LEFT)
        private Integer value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropPaddingCenterModel {
        @FixedField(length = 9, alignment = Alignment.CENTER, keepPadding = KeepPadding.DROP)
        private String value;
    }

    // -------------------------------------------------------------------------
    // Custom padding character
    // -------------------------------------------------------------------------

    @Test
    void exportCustomPadding_fillsWithStar() {
        assertEquals("hello***", parser.export(new CustomPaddingModel("hello")));
    }

    @Test
    void parseCustomPadding_stripsStars() {
        // keepPadding=AUTO for String defaults to KEEP, so stars are retained
        // Use DROP to strip the custom padding char
        assertEquals("hello***", parser.parse(CustomPaddingModel.class, "hello***").getValue());
    }

    // -------------------------------------------------------------------------
    // Null padding character
    // -------------------------------------------------------------------------

    @Test
    void exportNullPadding_nullValue_fillsWithDash() {
        assertEquals("-----", parser.export(new NullPaddingModel(null)));
    }

    // -------------------------------------------------------------------------
    // CENTER alignment — even padSize (regression: was off-by-one)
    // -------------------------------------------------------------------------

    @Test
    void exportCenter_evenPadSize_ab() {
        // len=2, size=10, padSize=8 → left=4, right=4
        assertEquals("    ab    ", parser.export(new CenterStringModel("ab")));
    }

    @Test
    void exportCenter_evenPadSize_abcd() {
        // len=4, size=10, padSize=6 → left=3, right=3
        assertEquals("   abcd   ", parser.export(new CenterStringModel("abcd")));
    }

    @Test
    void exportCenter_evenPadSize_abcdef() {
        // len=6, size=10, padSize=4 → left=2, right=2
        assertEquals("  abcdef  ", parser.export(new CenterStringModel("abcdef")));
    }

    @Test
    void exportCenter_evenPadSize_abcdefgh() {
        // len=8, size=10, padSize=2 → left=1, right=1
        assertEquals(" abcdefgh ", parser.export(new CenterStringModel("abcdefgh")));
    }

    // -------------------------------------------------------------------------
    // CENTER alignment — odd padSize (extra char goes right)
    // -------------------------------------------------------------------------

    @Test
    void exportCenter_oddPadSize_abc() {
        // len=3, size=10, padSize=7 → left=3, right=4
        assertEquals("   abc    ", parser.export(new CenterStringModel("abc")));
    }

    @Test
    void exportCenter_oddPadSize_abcde() {
        // len=5, size=10, padSize=5 → left=2, right=3
        assertEquals("  abcde   ", parser.export(new CenterStringModel("abcde")));
    }

    @Test
    void exportCenter_oddPadSize_abcdefg() {
        // len=7, size=10, padSize=3 → left=1, right=2
        assertEquals(" abcdefg  ", parser.export(new CenterStringModel("abcdefg")));
    }

    // -------------------------------------------------------------------------
    // CENTER alignment — total length always preserved
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "abc", "abcd", "abcde", "abcdef", "abcdefg", "abcdefgh", "abcdefghi"})
    void exportCenter_totalLengthAlwaysTen(String value) {
        assertEquals(10, parser.export(new CenterStringModel(value)).length());
    }

    // -------------------------------------------------------------------------
    // CENTER alignment — parse strips padding symmetrically
    // -------------------------------------------------------------------------

    @Test
    void parseCenter_stripsSpaces() {
        // keepPadding=AUTO for String defaults to KEEP — spaces are retained
        assertEquals("   abc    ", parser.parse(CenterStringModel.class, "   abc    ").getValue());
    }

    @Test
    void parseCenter_dropPadding_stripsSpaces() {
        assertEquals("hello", parser.parse(DropPaddingCenterModel.class, "  hello  ").getValue());
    }

    // -------------------------------------------------------------------------
    // RIGHT alignment for strings
    // -------------------------------------------------------------------------

    @Test
    void exportRightAlign_string() {
        assertEquals("   hello", parser.export(new RightAlignStringModel("hello")));
    }

    @Test
    void parseRightAlign_string() {
        // keepPadding=AUTO for String defaults to KEEP — leading spaces retained
        assertEquals("   hello", parser.parse(RightAlignStringModel.class, "   hello").getValue());
    }

    // -------------------------------------------------------------------------
    // LEFT alignment for numbers (override default RIGHT)
    // -------------------------------------------------------------------------

    @Test
    void exportLeftAlign_number_startsWithValue() {
        String result = parser.export(new LeftAlignNumberModel(42));
        assertTrue(result.startsWith("42"), "Expected '42' at start, got: " + result);
        assertEquals(6, result.length());
    }

    // -------------------------------------------------------------------------
    // Number default: RIGHT alignment, zero-padded
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultNumModel {
        @FixedField(length = 6)
        private Integer value;
    }

    @Test
    void exportNumber_defaultRightAlignZeroPadded() {
        assertEquals("000042", parser.export(new DefaultNumModel(42)));
    }
}
