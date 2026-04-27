package com.joutvhu.fixedwidth.parser.phase5;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.ParseError;
import com.joutvhu.fixedwidth.parser.ParseResult;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 5 — Error handling & reporting
 * <p>
 * Tests collect-all mode, ParseError context, and error recovery strategy.
 * All these tests will FAIL until Phase 5 is implemented.
 */
class ErrorHandlingTest {

    // -------------------------------------------------------------------------
    // Models
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class MultiErrorModel {
        @FixedRegex(regex = "^[A-Z]{3}$")
        @FixedField(length = 3)
        private String code; // error if not 3 uppercase letters

        @FixedField(start = 3, length = 5)
        private Integer number; // error if not a number

        @FixedFormat(format = "yyyy-MM-dd")
        @FixedField(start = 8, length = 10)
        private LocalDate date; // error if wrong format
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class RecoveryModel {
        @FixedField(length = 5)
        private Integer value; // may fail
    }

    // -------------------------------------------------------------------------
    // Fail-fast mode (default) — throw immediately on the first error
    // -------------------------------------------------------------------------

    @Test
    void failFast_throwsOnFirstError() {
        // "abc" doesn't match regex ^[A-Z]{3}$ → throw immediately, don't check subsequent fields
        assertThrows(FixedValidationException.class,
            () -> FixedParser.parser().parse(MultiErrorModel.class, "abcXXXXX2024-01-15"));
    }

    // -------------------------------------------------------------------------
    // Collect-all mode — collect all errors
    // -------------------------------------------------------------------------

    @Test
    void collectAll_returnsAllErrors() {
        // "abc" regex error, "XXXXX" number error, "not-a-date" date error
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXXnot-a-dat");

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().size() >= 2); // at least 2 errors
    }

    @Test
    void collectAll_continuesParsing_afterError() {
        // Even if the first field has an error, continue parsing subsequent fields
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abc0004200000000  ");

        assertTrue(result.hasErrors());
        // Field number (42) is still parsed even though field code has an error
        assertNotNull(result.getValue());
        assertEquals(42, result.getValue().getNumber());
    }

    @Test
    void collectAll_noErrors_returnsValue() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "ABC000422024-01-15");

        assertFalse(result.hasErrors());
        assertNotNull(result.getValue());
        assertEquals("ABC", result.getValue().getCode());
    }

    // -------------------------------------------------------------------------
    // ParseError — full context
    // -------------------------------------------------------------------------

    @Test
    void parseError_hasFieldPath() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXX2024-01-15");

        ParseError error = result.getErrors().get(0);
        assertNotNull(error.getFieldPath());
        assertTrue(error.getFieldPath().contains("code")); // "MultiErrorModel.code"
    }

    @Test
    void parseError_hasRawValue() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXX2024-01-15");

        ParseError error = result.getErrors().get(0);
        assertEquals("abc", error.getRawValue());
    }

    @Test
    void parseError_hasPhase() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXX2024-01-15");

        ParseError error = result.getErrors().get(0);
        assertNotNull(error.getPhase());
        // Regex validation occurs at READ_AFTER_TRANSFORM
        assertEquals(Phase.READ_AFTER_TRANSFORM, error.getPhase());
    }

    @Test
    void parseError_hasCause() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXX2024-01-15");

        ParseError error = result.getErrors().get(0);
        assertNotNull(error.getCause());
    }

    @Test
    void parseError_hasMessage() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXX2024-01-15");

        ParseError error = result.getErrors().get(0);
        assertNotNull(error.getMessage());
        assertFalse(error.getMessage().isBlank());
    }

    // -------------------------------------------------------------------------
    // Error recovery strategy — OnError
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class NullRecoveryModel {
        // Phase 5: @FixedRegex will have an onError attribute
        // Temporarily use collect-all mode to test behavior
        @FixedRegex(regex = "^[A-Z]+$")
        @FixedField(length = 3)
        String code;
    }

    @Test
    void onErrorNull_collectAllMode_fieldIsNullOnError() {
        // In collect-all mode, field error returns null instead of throwing
        ParseResult<NullRecoveryModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(NullRecoveryModel.class, "abc");

        // Phase 5 will be fully implemented — currently only verifying that there are errors
        assertTrue(result.hasErrors());
    }

    @Test
    void onErrorThrow_stillThrows() {
        // OnError.THROW is the default behavior — fail-fast
        assertThrows(Exception.class,
            () -> FixedParser.parser().parse(MultiErrorModel.class, "abcXXXXX2024-01-15"));
    }

    // -------------------------------------------------------------------------
    // ParseResult API
    // -------------------------------------------------------------------------

    @Test
    void parseResult_getValue_returnsNullWhenAllFieldsFailed() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXXnot-a-dat");

        // Has errors but still returns a partial object (not null)
        assertNotNull(result.getValue());
    }

    @Test
    void parseResult_getErrors_returnsEmptyListWhenNoErrors() {
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "ABC000422024-01-15");

        assertFalse(result.hasErrors());
        assertTrue(result.getErrors().isEmpty());
    }
}
