package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.exception.NoHookFoundException;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.DefaultContextFrame;
import com.joutvhu.fixedwidth.parser.support.DefaultParseContext;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.FrameType;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tasks 5.1–5.4 — Property tests and unit tests for invokeHooks() dispatch.
 * Requirements: 2.2, 2.3, 4.1, 4.4, 6.2, 10.3, 10.5, 10.6
 */
class InvokeHooksDispatchTest {

    static final List<String> callLog = new ArrayList<>();

    static class SupportingHookA implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) {
            return true;
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            callLog.add("A");
        }
    }

    static class SupportingHookB implements ModuleHook {
        @Override
        public boolean supports(FixedTypeInfo info) {
            return true;
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            callLog.add("B");
        }
    }

    static class NotSupportingHook implements ModuleHook {
        static int handleCallCount = 0;

        @Override
        public boolean supports(FixedTypeInfo info) {
            return false;
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            handleCallCount++;
        }
    }

    static class TestModule extends FixedModule {
        TestModule() {
            super();
        }

        TestModule(Class<?>... classes) {
            super(classes);
        }
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class SimpleModel {
        @FixedField(length = 5)
        String value;
    }

    static DefaultParseContext makeCtx(Phase phase) {
        DefaultParseContext ctx = new DefaultParseContext(phase,
            Collections.emptyMap(), Collections.emptyMap());
        FixedTypeInfo info = FixedTypeInfo.of(SimpleModel.class);
        DefaultContextFrame frame = new DefaultContextFrame(
            info, FrameType.FIELD, 0, -1, null, null, null, null);
        ctx.pushFrame(frame);
        return ctx;
    }

    @BeforeEach
    void setUp() {
        callLog.clear();
        NotSupportingHook.handleCallCount = 0;
    }

    // ── Property 3: Supports consistency ─────────────────────────────────────
    // FOR ALL module-hook H and field info I, if H.supports(I) = false then
    // H.handle() is never called — verified across every Phase value.

    @ParameterizedTest
    @EnumSource(Phase.class)
    void property3_supportsConsistency_handleNeverCalledWhenSupportsFalse(Phase phase) {
        TestModule module = new TestModule();
        module.registerHook(NotSupportingHook.class);
        module.registerHook(SupportingHookA.class); // fallback so no NoHookFoundException

        DefaultParseContext ctx = makeCtx(phase);
        FixedTypeInfo info = FixedTypeInfo.of(SimpleModel.class);

        module.invokeHooks(info, ctx);

        assertEquals(0, NotSupportingHook.handleCallCount,
            "handle() must not be called when supports() returns false at phase " + phase);
    }

    // ── Property 6: skipCurrentField idempotence ──────────────────────────────
    // Calling skipCurrentField() multiple times must not throw and must leave
    // isSkipField() = true.

    @Test
    void property6_skipCurrentField_calledMultipleTimes_noError() {
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM);
        assertDoesNotThrow(() -> {
            ctx.skipCurrentField();
            ctx.skipCurrentField();
            ctx.skipCurrentField();
        });
        assertTrue(ctx.isSkipField());
    }

    // ── Unit tests for invokeHooks() dispatch ─────────────────────────────────

    @Test
    void invokeHooks_noModuleHookSupports_throwsNoHookFoundException() {
        TestModule module = new TestModule();
        module.registerHook(NotSupportingHook.class);

        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM);
        FixedTypeInfo info = FixedTypeInfo.of(SimpleModel.class);

        assertThrows(NoHookFoundException.class, () -> module.invokeHooks(info, ctx));
    }

    @Test
    void invokeHooks_notSupportingHook_handleNeverCalled() {
        TestModule module = new TestModule();
        module.registerHook(NotSupportingHook.class);
        module.registerHook(SupportingHookA.class);

        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM);
        FixedTypeInfo info = FixedTypeInfo.of(SimpleModel.class);

        module.invokeHooks(info, ctx);
        assertEquals(0, NotSupportingHook.handleCallCount);
    }

    @Test
    void invokeHooks_noHooksRegistered_throwsNoHookFoundException() {
        TestModule module = new TestModule();
        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM);
        FixedTypeInfo info = FixedTypeInfo.of(SimpleModel.class);

        assertThrows(NoHookFoundException.class, () -> module.invokeHooks(info, ctx));
    }

    @Test
    void invokeHooks_firstSupportingHookCalled_secondSkipped() {
        // First-match semantics: only the first supporting hook should be called.
        TestModule module = new TestModule(SupportingHookA.class, SupportingHookB.class);

        DefaultParseContext ctx = makeCtx(Phase.READ_AFTER_TRANSFORM);
        FixedTypeInfo info = FixedTypeInfo.of(SimpleModel.class);

        module.invokeHooks(info, ctx);

        assertEquals(1, callLog.size());
        assertEquals("A", callLog.get(0));
    }

    @Test
    void invokeHooks_nullContext_doesNotThrow() {
        // invokeHooks() must guard against null ctx and return silently.
        TestModule module = new TestModule(SupportingHookA.class);
        FixedTypeInfo info = FixedTypeInfo.of(SimpleModel.class);

        assertDoesNotThrow(() -> module.invokeHooks(info, null));
    }
}
