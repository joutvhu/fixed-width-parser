package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;
import com.joutvhu.fixedwidth.parser.annotation.FixedEncoding;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.convert.handler.BooleanHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.ConditionalHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.DateHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.EncodingHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.FormatDispatchHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.NumberHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.OptionHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.RegexHandler;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import com.joutvhu.fixedwidth.parser.exception.RegexMismatchException;
import com.joutvhu.fixedwidth.parser.support.DefaultContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.FrameType;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Task 10.2 — Unit tests verifying the 8 annotation-handlers are correctly migrated to Hook.
 *
 * <p>Each test verifies:
 * <ol>
 *   <li>The handler implements {@link Hook} (not the old AnnotationHandler)</li>
 *   <li>{@code getSupportedPhases()} returns the correct phases</li>
 *   <li>{@code handle()} works correctly with the new interface</li>
 * </ol>
 */
class AnnotationHandlerMigrationTest {

    // ── Test models ───────────────────────────────────────────────────────────

    @FixedObject
    @Data
    @NoArgsConstructor
    static class RegexModel {
        @FixedRegex(regex = "[A-Z]+")
        @FixedField(length = 5)
        String code;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class OptionModel {
        @FixedOption(options = {"Y", "N"})
        @FixedField(length = 1)
        String flag;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class ConditionalModel {
        @FixedField(length = 1)
        String type;

        @FixedConditional(dependsOnField = "type", whenValue = "A")
        @FixedField(length = 5)
        String data;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class DateFormatModel {
        @FixedFormat(format = "yyyy-MM-dd")
        @FixedField(length = 10)
        LocalDate date;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class BoolFormatModel {
        @FixedFormat(format = "Y|N")
        @FixedField(length = 1)
        Boolean active;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class NumberFormatModel {
        @FixedFormat(format = "###")
        @FixedField(length = 3)
        Integer count;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class EncodingModel {
        @FixedEncoding("ISO-8859-1")
        @FixedField(length = 10)
        String name;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static FixedTypeInfo fieldInfo(Class<?> model, String field) throws Exception {
        return FixedTypeInfo.of(model.getDeclaredField(field));
    }

    static DefaultParseContext makeCtx(Phase phase, String processedString) {
        DefaultParseContext ctx = new DefaultParseContext(phase,
            Collections.emptyMap(), Collections.emptyMap());
        DefaultContextFrame frame = new DefaultContextFrame(
            null, FrameType.FIELD, 0, -1, null, processedString, null, null);
        ctx.pushFrame(frame);
        return ctx;
    }

    static DefaultParseContext makeWriteCtx(Phase phase, Object value) {
        DefaultParseContext ctx = new DefaultParseContext(phase,
            Collections.emptyMap(), Collections.emptyMap());
        DefaultContextFrame frame = new DefaultContextFrame(
            null, FrameType.FIELD, 0, -1, null, null, null, null);
        ctx.pushFrame(frame);
        ctx.setCurrentValue(value);
        return ctx;
    }

    // ── RegexHandler ──────────────────────────────────────────────────────────

    @Test
    void regexHandler_implementsHook() {
        assertInstanceOf(Hook.class, new RegexHandler());
    }

    @Test
    void regexHandler_supportedPhases() {
        RegexHandler handler = new RegexHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_AFTER_TRANSFORM));
        assertTrue(handler.getSupportedPhases().contains(Phase.WRITE_AFTER_TRANSFORM));
        assertEquals(2, handler.getSupportedPhases().size());
    }

    @Test
    void regexHandler_validValue_noException() throws Exception {
        RegexHandler handler = new RegexHandler();
        FixedTypeInfo info = fieldInfo(RegexModel.class, "code");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "HELLO");
        assertDoesNotThrow(() -> handler.handle(info, ctx));
    }

    @Test
    void regexHandler_invalidValue_throwsException() throws Exception {
        RegexHandler handler = new RegexHandler();
        FixedTypeInfo info = fieldInfo(RegexModel.class, "code");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "hello");
        assertThrows(RegexMismatchException.class, () -> handler.handle(info, ctx));
    }

    @Test
    void regexHandler_writePhase_invalidValue_throwsException() throws Exception {
        RegexHandler handler = new RegexHandler();
        FixedTypeInfo info = fieldInfo(RegexModel.class, "code");
        DefaultParseContext ctx = makeWriteCtx(Phase.WRITE_AFTER_TRANSFORM, "hello");
        assertThrows(RegexMismatchException.class, () -> handler.handle(info, ctx));
    }

    // ── OptionHandler ─────────────────────────────────────────────────────────

    @Test
    void optionHandler_implementsHook() {
        assertInstanceOf(Hook.class, new OptionHandler());
    }

    @Test
    void optionHandler_supportedPhases() {
        OptionHandler handler = new OptionHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_AFTER_TRANSFORM));
        assertTrue(handler.getSupportedPhases().contains(Phase.WRITE_AFTER_TRANSFORM));
        assertEquals(2, handler.getSupportedPhases().size());
    }

    @Test
    void optionHandler_validOption_noException() throws Exception {
        OptionHandler handler = new OptionHandler();
        FixedTypeInfo info = fieldInfo(OptionModel.class, "flag");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "Y");
        assertDoesNotThrow(() -> handler.handle(info, ctx));
    }

    @Test
    void optionHandler_invalidOption_throwsException() throws Exception {
        OptionHandler handler = new OptionHandler();
        FixedTypeInfo info = fieldInfo(OptionModel.class, "flag");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "X");
        assertThrows(FixedValidationException.class, () -> handler.handle(info, ctx));
    }

    // ── ConditionalHandler ────────────────────────────────────────────────────

    @Test
    void conditionalHandler_implementsHook() {
        assertInstanceOf(Hook.class, new ConditionalHandler());
    }

    @Test
    void conditionalHandler_supportedPhases() {
        ConditionalHandler handler = new ConditionalHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_PRE_CUT));
        assertTrue(handler.getSupportedPhases().contains(Phase.WRITE_PRE_GET));
        assertEquals(2, handler.getSupportedPhases().size());
    }

    @Test
    void conditionalHandler_noAnnotation_doesNotSkip() throws Exception {
        // A field without @FixedConditional — handler is a no-op
        ConditionalHandler handler = new ConditionalHandler();
        FixedTypeInfo info = fieldInfo(ConditionalModel.class, "type"); // no @FixedConditional
        DefaultParseContext ctx = makeCtx(Phase.READ_PRE_CUT, null);
        handler.handle(info, ctx);
        assertFalse(ctx.isSkipField());
    }

    // ── FormatDispatchHandler ─────────────────────────────────────────────────

    @Test
    void formatDispatchHandler_implementsHook() {
        assertInstanceOf(Hook.class, new FormatDispatchHandler());
    }

    @Test
    void formatDispatchHandler_supportedPhases() {
        FormatDispatchHandler handler = new FormatDispatchHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_AFTER_TRANSFORM));
        assertTrue(handler.getSupportedPhases().contains(Phase.WRITE_AFTER_TRANSFORM));
        assertEquals(2, handler.getSupportedPhases().size());
    }

    @Test
    void formatDispatchHandler_dateField_invalidFormat_throwsException() throws Exception {
        FormatDispatchHandler handler = new FormatDispatchHandler();
        FixedTypeInfo info = fieldInfo(DateFormatModel.class, "date");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "not-a-date");
        assertThrows(Exception.class, () -> handler.handle(info, ctx));
    }

    @Test
    void formatDispatchHandler_boolField_invalidFormat_throwsException() throws Exception {
        FormatDispatchHandler handler = new FormatDispatchHandler();
        FixedTypeInfo info = fieldInfo(BoolFormatModel.class, "active");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "X");
        assertThrows(FixedValidationException.class, () -> handler.handle(info, ctx));
    }

    // ── BooleanHandler ────────────────────────────────────────────────────────

    @Test
    void booleanHandler_implementsHook() {
        assertInstanceOf(Hook.class, new BooleanHandler());
    }

    @Test
    void booleanHandler_supportedPhases() {
        BooleanHandler handler = new BooleanHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_AFTER_TRANSFORM));
        assertEquals(1, handler.getSupportedPhases().size());
    }

    @Test
    void booleanHandler_validValue_noException() throws Exception {
        BooleanHandler handler = new BooleanHandler();
        FixedTypeInfo info = fieldInfo(BoolFormatModel.class, "active");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "Y");
        assertDoesNotThrow(() -> handler.handle(info, ctx));
    }

    @Test
    void booleanHandler_invalidValue_throwsException() throws Exception {
        BooleanHandler handler = new BooleanHandler();
        FixedTypeInfo info = fieldInfo(BoolFormatModel.class, "active");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "X");
        assertThrows(FixedValidationException.class, () -> handler.handle(info, ctx));
    }

    // ── DateHandler ───────────────────────────────────────────────────────────

    @Test
    void dateHandler_implementsHook() {
        assertInstanceOf(Hook.class, new DateHandler());
    }

    @Test
    void dateHandler_supportedPhases() {
        DateHandler handler = new DateHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_AFTER_TRANSFORM));
        assertEquals(1, handler.getSupportedPhases().size());
    }

    @Test
    void dateHandler_validDate_noException() throws Exception {
        DateHandler handler = new DateHandler();
        FixedTypeInfo info = fieldInfo(DateFormatModel.class, "date");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "2024-01-15");
        assertDoesNotThrow(() -> handler.handle(info, ctx));
    }

    @Test
    void dateHandler_invalidDate_throwsException() throws Exception {
        DateHandler handler = new DateHandler();
        FixedTypeInfo info = fieldInfo(DateFormatModel.class, "date");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "not-a-date");
        assertThrows(Exception.class, () -> handler.handle(info, ctx));
    }

    // ── NumberHandler ─────────────────────────────────────────────────────────

    @Test
    void numberHandler_implementsHook() {
        assertInstanceOf(Hook.class, new NumberHandler());
    }

    @Test
    void numberHandler_supportedPhases() {
        NumberHandler handler = new NumberHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_AFTER_TRANSFORM));
        assertEquals(1, handler.getSupportedPhases().size());
    }

    @Test
    void numberHandler_validNumber_noException() throws Exception {
        NumberHandler handler = new NumberHandler();
        FixedTypeInfo info = fieldInfo(NumberFormatModel.class, "count");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "123");
        assertDoesNotThrow(() -> handler.handle(info, ctx));
    }

    @Test
    void numberHandler_invalidNumber_throwsException() throws Exception {
        NumberHandler handler = new NumberHandler();
        FixedTypeInfo info = fieldInfo(NumberFormatModel.class, "count");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM, "abc");
        assertThrows(FixedValidationException.class, () -> handler.handle(info, ctx));
    }

    // ── EncodingHandler ───────────────────────────────────────────────────────

    @Test
    void encodingHandler_implementsHook() {
        assertInstanceOf(Hook.class, new EncodingHandler());
    }

    @Test
    void encodingHandler_supportedPhases() {
        EncodingHandler handler = new EncodingHandler();
        assertTrue(handler.getSupportedPhases().contains(Phase.READ_AFTER_CUT));
        assertTrue(handler.getSupportedPhases().contains(Phase.WRITE_AFTER_CONVERT));
        assertEquals(2, handler.getSupportedPhases().size());
    }

    @Test
    void encodingHandler_readPhase_noException() throws Exception {
        EncodingHandler handler = new EncodingHandler();
        FixedTypeInfo info = fieldInfo(EncodingModel.class, "name");
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_CUT, "hello");
        assertDoesNotThrow(() -> handler.handle(info, ctx));
    }

    @Test
    void encodingHandler_writePhase_noException() throws Exception {
        EncodingHandler handler = new EncodingHandler();
        FixedTypeInfo info = fieldInfo(EncodingModel.class, "name");
        DefaultParseContext ctx = makeWriteCtx(Phase.WRITE_AFTER_CONVERT, "hello");
        assertDoesNotThrow(() -> handler.handle(info, ctx));
    }
}
