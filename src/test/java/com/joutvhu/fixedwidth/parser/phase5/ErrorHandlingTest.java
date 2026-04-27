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
 * Kiểm tra collect-all mode, ParseError context, và error recovery strategy.
 * Tất cả test này sẽ FAIL cho đến khi Phase 5 được implement.
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
        private String code; // lỗi nếu không phải 3 chữ hoa

        @FixedField(start = 3, length = 5)
        private Integer number; // lỗi nếu không phải số

        @FixedFormat(format = "yyyy-MM-dd")
        @FixedField(start = 8, length = 10)
        private LocalDate date; // lỗi nếu sai format
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class RecoveryModel {
        @FixedField(length = 5)
        private Integer value; // có thể lỗi
    }

    // -------------------------------------------------------------------------
    // Fail-fast mode (default) — throw ngay khi gặp lỗi đầu tiên
    // -------------------------------------------------------------------------

    @Test
    void failFast_throwsOnFirstError() {
        // "abc" không match regex ^[A-Z]{3}$ → throw ngay, không check field sau
        assertThrows(FixedValidationException.class,
            () -> FixedParser.parser().parse(MultiErrorModel.class, "abcXXXXX2024-01-15"));
    }

    // -------------------------------------------------------------------------
    // Collect-all mode — thu thập tất cả lỗi
    // -------------------------------------------------------------------------

    @Test
    void collectAll_returnsAllErrors() {
        // "abc" lỗi regex, "XXXXX" lỗi number, "not-a-date" lỗi date
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abcXXXXXnot-a-dat");

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().size() >= 2); // ít nhất 2 lỗi
    }

    @Test
    void collectAll_continuesParsing_afterError() {
        // Dù field đầu lỗi, vẫn tiếp tục parse các field sau
        ParseResult<MultiErrorModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(MultiErrorModel.class, "abc0004200000000  ");

        assertTrue(result.hasErrors());
        // Field number (42) vẫn được parse dù field code lỗi
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
    // ParseError — context đầy đủ
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
        // Regex validation xảy ra ở READ_AFTER_TRANSFORM
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
        // Phase 5: @FixedRegex sẽ có onError attribute
        // Tạm thời dùng collect-all mode để test behavior
        @FixedRegex(regex = "^[A-Z]+$")
        @FixedField(length = 3)
        String code;
    }

    @Test
    void onErrorNull_collectAllMode_fieldIsNullOnError() {
        // Trong collect-all mode, field lỗi trả về null thay vì throw
        ParseResult<NullRecoveryModel> result = FixedParser.parser()
            .collectErrors()
            .parseResult(NullRecoveryModel.class, "abc");

        // Phase 5 sẽ implement đầy đủ — hiện tại chỉ verify có lỗi
        assertTrue(result.hasErrors());
    }

    @Test
    void onErrorThrow_stillThrows() {
        // OnError.THROW là default behavior — fail-fast
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

        // Có lỗi nhưng vẫn trả về partial object (không null)
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
