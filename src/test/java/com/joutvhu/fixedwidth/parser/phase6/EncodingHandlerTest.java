package com.joutvhu.fixedwidth.parser.phase6;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedEncoding;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6 — @FixedEncoding per-field encoding
 */
class EncodingHandlerTest {

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class EncodedModel {
        @FixedEncoding("UTF-8")
        @FixedField(start = 0, length = 10)
        private String utf8Field;

        @FixedField(start = 10, length = 5)
        private Integer amount;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class InvalidEncodingModel {
        @FixedEncoding("NOT-A-CHARSET")
        @FixedField(start = 0, length = 5)
        private String field;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class NoEncodingModel {
        @FixedField(start = 0, length = 10)
        private String field;

        @FixedField(start = 10, length = 5)
        private Integer amount;
    }

    // ── UTF-8 (same as platform default on most systems) ─────────────────────

    @Test
    void utf8Encoding_sameAsPlatformDefault_parseSucceeds() {
        // UTF-8 is typically the platform default — handler is a no-op
        EncodedModel result = FixedParser.parser()
                .parse(EncodedModel.class, "hello     00042");

        assertNotNull(result);
        assertEquals(42, result.getAmount());
    }

    @Test
    void utf8Encoding_exportSucceeds() {
        EncodedModel model = new EncodedModel();
        model.setUtf8Field("hello");
        model.setAmount(42);

        assertDoesNotThrow(() -> FixedParser.parser().export(model));
    }

    // ── Invalid charset ───────────────────────────────────────────────────────

    @Test
    void invalidCharset_throwsFixedParserException() {
        assertThrows(FixedParserException.class,
                () -> FixedParser.parser()
                        .parse(InvalidEncodingModel.class, "hello"));
    }

    // ── No encoding annotation — baseline ────────────────────────────────────

    @Test
    void noEncoding_parseSucceeds() {
        NoEncodingModel result = FixedParser.parser()
                .parse(NoEncodingModel.class, "hello     00042");

        assertNotNull(result);
        assertEquals(42, result.getAmount());
    }

    @Test
    void noEncoding_exportSucceeds() {
        NoEncodingModel model = new NoEncodingModel();
        model.setField("hello");
        model.setAmount(42);

        String result = FixedParser.parser().export(model);
        assertNotNull(result);
        assertEquals(15, result.length());
    }

    // ── Round-trip ────────────────────────────────────────────────────────────

    @Test
    void utf8_roundTrip_valuePreserved() {
        NoEncodingModel original = new NoEncodingModel();
        original.setField("ABCDE");
        original.setAmount(123);

        String exported = FixedParser.parser().export(original);
        NoEncodingModel parsed = FixedParser.parser().parse(NoEncodingModel.class, exported);

        assertEquals(123, parsed.getAmount());
    }
}
