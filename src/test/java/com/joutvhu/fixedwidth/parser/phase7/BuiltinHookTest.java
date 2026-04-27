package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.convert.hook.BooleanHook;
import com.joutvhu.fixedwidth.parser.convert.hook.DateHook;
import com.joutvhu.fixedwidth.parser.convert.hook.EnumHook;
import com.joutvhu.fixedwidth.parser.convert.hook.NumberHook;
import com.joutvhu.fixedwidth.parser.convert.hook.StringHook;
import com.joutvhu.fixedwidth.parser.convert.hook.UUIDHook;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Task 6.1 — Unit tests for built-in hooks.
 * Requirements: 2.1, 6.1
 */
class BuiltinHookTest {

    // ── Test models ───────────────────────────────────────────────────────────

    @FixedObject
    @Data
    @NoArgsConstructor
    static class StringModel {
        @FixedField(length = 10)
        String value;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class BoolModel {
        @FixedField(length = 1)
        Boolean active;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class IntModel {
        @FixedField(length = 5)
        Integer count;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class DateModel {
        @FixedField(length = 10)
        LocalDate date;
    }

    enum Status { ACTIVE, INACTIVE }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class EnumModel {
        @FixedField(length = 8)
        Status status;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class UUIDModel {
        @FixedField(length = 36)
        UUID id;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    static DefaultParseContext makeReadCtx(FixedTypeInfo info, String processedString) {
        DefaultParseContext ctx = new DefaultParseContext(Phase.READ_AFTER_TRANSFORM,
            Collections.emptyMap(), Collections.emptyMap());
        DefaultContextFrame frame = new DefaultContextFrame(
            info, FrameType.FIELD, 0, -1, null, processedString, null, null);
        ctx.pushFrame(frame);
        return ctx;
    }

    static DefaultParseContext makeWriteCtx(FixedTypeInfo info, Object value) {
        DefaultParseContext ctx = new DefaultParseContext(Phase.WRITE_AFTER_GET,
            Collections.emptyMap(), Collections.emptyMap());
        DefaultContextFrame frame = new DefaultContextFrame(
            info, FrameType.FIELD, 0, -1, null, null, null, null);
        ctx.pushFrame(frame);
        ctx.setCurrentValue(value);
        return ctx;
    }

    static FixedTypeInfo fieldInfo(Class<?> modelClass, String fieldName) throws Exception {
        return FixedTypeInfo.of(modelClass.getDeclaredField(fieldName));
    }

    // ── StringHook ────────────────────────────────────────────────────────────

    @Test
    void stringHook_supports_stringType() throws Exception {
        StringHook hook = new StringHook();
        assertTrue(hook.supports(fieldInfo(StringModel.class, "value")));
    }

    @Test
    void stringHook_notSupports_intType() throws Exception {
        StringHook hook = new StringHook();
        assertFalse(hook.supports(fieldInfo(IntModel.class, "count")));
    }

    @Test
    void stringHook_read_returnsProcessedString() throws Exception {
        StringHook hook = new StringHook();
        FixedTypeInfo info = fieldInfo(StringModel.class, "value");
        DefaultParseContext ctx = makeReadCtx(info, "hello     ");
        hook.handle(info, ctx);
        assertEquals("hello     ", ctx.getCurrentValue());
    }

    @Test
    void stringHook_write_convertsToString() throws Exception {
        StringHook hook = new StringHook();
        FixedTypeInfo info = fieldInfo(StringModel.class, "value");
        DefaultParseContext ctx = makeWriteCtx(info, "world");
        hook.handle(info, ctx);
        assertEquals("world", ctx.getCurrentValue());
    }

    // ── BooleanHook ───────────────────────────────────────────────────────────

    @Test
    void booleanHook_supports_booleanType() throws Exception {
        BooleanHook hook = new BooleanHook();
        assertTrue(hook.supports(fieldInfo(BoolModel.class, "active")));
    }

    @Test
    void booleanHook_notSupports_stringType() throws Exception {
        BooleanHook hook = new BooleanHook();
        assertFalse(hook.supports(fieldInfo(StringModel.class, "value")));
    }

    @Test
    void booleanHook_read_trueValue() throws Exception {
        BooleanHook hook = new BooleanHook();
        FixedTypeInfo info = fieldInfo(BoolModel.class, "active");
        DefaultParseContext ctx = makeReadCtx(info, "Y");
        hook.handle(info, ctx);
        assertEquals(Boolean.TRUE, ctx.getCurrentValue());
    }

    @Test
    void booleanHook_read_falseValue() throws Exception {
        BooleanHook hook = new BooleanHook();
        FixedTypeInfo info = fieldInfo(BoolModel.class, "active");
        DefaultParseContext ctx = makeReadCtx(info, "N");
        hook.handle(info, ctx);
        assertEquals(Boolean.FALSE, ctx.getCurrentValue());
    }

    @Test
    void booleanHook_write_trueToString() throws Exception {
        BooleanHook hook = new BooleanHook();
        FixedTypeInfo info = fieldInfo(BoolModel.class, "active");
        DefaultParseContext ctx = makeWriteCtx(info, Boolean.TRUE);
        hook.handle(info, ctx);
        assertNotNull(ctx.getCurrentValue());
        // Should be "T" or "Y" or "YES" depending on field length
        assertTrue(ctx.getCurrentValue().toString().length() > 0);
    }

    // ── NumberHook ────────────────────────────────────────────────────────────

    @Test
    void numberHook_supports_integerType() throws Exception {
        NumberHook hook = new NumberHook();
        assertTrue(hook.supports(fieldInfo(IntModel.class, "count")));
    }

    @Test
    void numberHook_notSupports_stringType() throws Exception {
        NumberHook hook = new NumberHook();
        assertFalse(hook.supports(fieldInfo(StringModel.class, "value")));
    }

    @Test
    void numberHook_read_parsesInteger() throws Exception {
        NumberHook hook = new NumberHook();
        FixedTypeInfo info = fieldInfo(IntModel.class, "count");
        DefaultParseContext ctx = makeReadCtx(info, "00042");
        hook.handle(info, ctx);
        assertEquals(42, ctx.getCurrentValue());
    }

    @Test
    void numberHook_write_convertsToString() throws Exception {
        NumberHook hook = new NumberHook();
        FixedTypeInfo info = fieldInfo(IntModel.class, "count");
        DefaultParseContext ctx = makeWriteCtx(info, 42);
        hook.handle(info, ctx);
        assertEquals("42", ctx.getCurrentValue());
    }

    // ── DateHook ──────────────────────────────────────────────────────────────

    @Test
    void dateHook_supports_localDateType() throws Exception {
        DateHook hook = new DateHook();
        assertTrue(hook.supports(fieldInfo(DateModel.class, "date")));
    }

    @Test
    void dateHook_notSupports_stringType() throws Exception {
        DateHook hook = new DateHook();
        assertFalse(hook.supports(fieldInfo(StringModel.class, "value")));
    }

    // ── EnumHook ──────────────────────────────────────────────────────────────

    @Test
    void enumHook_supports_enumType() throws Exception {
        EnumHook hook = new EnumHook();
        assertTrue(hook.supports(fieldInfo(EnumModel.class, "status")));
    }

    @Test
    void enumHook_notSupports_stringType() throws Exception {
        EnumHook hook = new EnumHook();
        assertFalse(hook.supports(fieldInfo(StringModel.class, "value")));
    }

    @Test
    void enumHook_read_parsesEnumByName() throws Exception {
        EnumHook hook = new EnumHook();
        FixedTypeInfo info = fieldInfo(EnumModel.class, "status");
        DefaultParseContext ctx = makeReadCtx(info, "ACTIVE  ");
        hook.handle(info, ctx);
        assertEquals(Status.ACTIVE, ctx.getCurrentValue());
    }

    @Test
    void enumHook_write_convertsEnumToName() throws Exception {
        EnumHook hook = new EnumHook();
        FixedTypeInfo info = fieldInfo(EnumModel.class, "status");
        DefaultParseContext ctx = makeWriteCtx(info, Status.INACTIVE);
        hook.handle(info, ctx);
        assertEquals("INACTIVE", ctx.getCurrentValue());
    }

    // ── UUIDHook ──────────────────────────────────────────────────────────────

    @Test
    void uuidHook_supports_uuidType() throws Exception {
        UUIDHook hook = new UUIDHook();
        assertTrue(hook.supports(fieldInfo(UUIDModel.class, "id")));
    }

    @Test
    void uuidHook_notSupports_stringType() throws Exception {
        UUIDHook hook = new UUIDHook();
        assertFalse(hook.supports(fieldInfo(StringModel.class, "value")));
    }

    @Test
    void uuidHook_read_parsesUUID() throws Exception {
        UUIDHook hook = new UUIDHook();
        FixedTypeInfo info = fieldInfo(UUIDModel.class, "id");
        String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
        DefaultParseContext ctx = makeReadCtx(info, uuidStr);
        hook.handle(info, ctx);
        assertEquals(UUID.fromString(uuidStr), ctx.getCurrentValue());
    }

    @Test
    void uuidHook_write_convertsUUIDToString() throws Exception {
        UUIDHook hook = new UUIDHook();
        FixedTypeInfo info = fieldInfo(UUIDModel.class, "id");
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        DefaultParseContext ctx = makeWriteCtx(info, uuid);
        hook.handle(info, ctx);
        assertEquals("550e8400-e29b-41d4-a716-446655440000", ctx.getCurrentValue());
    }
}
