package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.convert.hook.BooleanHook;
import com.joutvhu.fixedwidth.parser.convert.hook.CollectionHook;
import com.joutvhu.fixedwidth.parser.convert.hook.DateHook;
import com.joutvhu.fixedwidth.parser.convert.hook.EnumHook;
import com.joutvhu.fixedwidth.parser.convert.hook.MapHook;
import com.joutvhu.fixedwidth.parser.convert.hook.NumberHook;
import com.joutvhu.fixedwidth.parser.convert.hook.ObjectHook;
import com.joutvhu.fixedwidth.parser.convert.hook.OptionalHook;
import com.joutvhu.fixedwidth.parser.convert.hook.StringHook;
import com.joutvhu.fixedwidth.parser.convert.hook.UUIDHook;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Task 9.1 — Property 2: Phase isolation.
 *
 * <p>FOR ALL hook H registered at phase P, {@code H.handle()} is called exactly once
 * at phase P and NOT called at any phase not in {@code phases()}.
 *
 * <p><b>Validates: Requirements 1.3, 8.1, 8.2, 10.2</b>
 */
class PhaseIsolationTest {

    // ── Simple model for parsing ──────────────────────────────────────────────

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class SimpleModel {
        @FixedField(length = 10)
        String value;
    }

    // ── Tracking hook — only supports READ_AFTER_TRANSFORM ───────────────────

    static final List<Phase> calledAtPhases = new ArrayList<>();

    /**
     * A ModuleHook that:
     * - supports String fields only
     * - declares phases() = {READ_AFTER_TRANSFORM}
     * - records every phase it is actually called at
     */
    public static class TrackingHook implements ModuleHook {

        public TrackingHook() {
        }

        @Override
        public boolean supports(FixedTypeInfo info) {
            return String.class.equals(info.getType());
        }

        @Override
        public Set<Phase> phases() {
            return EnumSet.of(Phase.READ_AFTER_TRANSFORM);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            calledAtPhases.add(ctx.getPhase());
            // Set the value so the pipeline can continue
            ctx.setCurrentValue(ctx.getProcessedString());
        }
    }

    /**
     * A ModuleHook that only supports WRITE_AFTER_GET — should never be called during read.
     */
    public static class WriteOnlyHook implements ModuleHook {

        public WriteOnlyHook() {
        }

        @Override
        public boolean supports(FixedTypeInfo info) {
            return String.class.equals(info.getType());
        }

        @Override
        public Set<Phase> phases() {
            return EnumSet.of(Phase.WRITE_AFTER_GET);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            writeOnlyHookCalls.add(ctx.getPhase());
        }
    }

    static final List<Phase> writeOnlyHookCalls = new ArrayList<>();

    // ── Custom modules ────────────────────────────────────────────────────────

    /**
     * Module with TrackingHook registered FIRST (highest priority),
     * followed by all default hooks as fallback.
     */
    public static class TrackingModule extends FixedModule {
        public TrackingModule() {
            super();
            // TrackingHook first — it handles String at READ_AFTER_TRANSFORM only
            registerHook(TrackingHook.class);
            // Default hooks as fallback for other types and phases
            registerHook(BooleanHook.class);
            registerHook(NumberHook.class);
            registerHook(DateHook.class);
            registerHook(EnumHook.class);
            registerHook(UUIDHook.class);
            registerHook(OptionalHook.class);
            registerHook(CollectionHook.class);
            registerHook(MapHook.class);
            registerHook(ObjectHook.class);
        }
    }

    /**
     * Module with WriteOnlyHook registered FIRST, followed by default hooks.
     */
    public static class WriteOnlyModule extends FixedModule {
        public WriteOnlyModule() {
            super();
            registerHook(WriteOnlyHook.class);
            registerHook(StringHook.class);
            registerHook(BooleanHook.class);
            registerHook(NumberHook.class);
            registerHook(DateHook.class);
            registerHook(EnumHook.class);
            registerHook(UUIDHook.class);
            registerHook(OptionalHook.class);
            registerHook(CollectionHook.class);
            registerHook(MapHook.class);
            registerHook(ObjectHook.class);
        }
    }

    @BeforeEach
    void setUp() {
        calledAtPhases.clear();
        writeOnlyHookCalls.clear();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * Property 2 — Phase isolation:
     * The TrackingHook declares only READ_AFTER_TRANSFORM as its supported phase.
     * After a full parse, handle() must have been called exactly once (at READ_AFTER_TRANSFORM)
     * and never at any other phase.
     */
    @Test
    void property2_phaseIsolation_hookCalledOnlyAtDeclaredPhase() {
        FixedParser parser = FixedParser.parser().use(new TrackingModule());
        SimpleModel result = parser.parse(SimpleModel.class, "hello     ");

        assertNotNull(result);

        // Hook must have been called exactly once
        assertEquals(1, calledAtPhases.size(),
            "handle() should be called exactly once, but was called at: " + calledAtPhases);

        // That one call must be at READ_AFTER_TRANSFORM
        assertEquals(Phase.READ_AFTER_TRANSFORM, calledAtPhases.get(0),
            "handle() must be called at READ_AFTER_TRANSFORM");
    }

    /**
     * Verify that no call happened at any phase outside phases().
     */
    @Test
    void property2_hookNeverCalledAtUnsupportedPhases() {
        FixedParser parser = FixedParser.parser().use(new TrackingModule());
        parser.parse(SimpleModel.class, "world     ");

        Set<Phase> supported = EnumSet.of(Phase.READ_AFTER_TRANSFORM);
        for (Phase phase : calledAtPhases) {
            assertTrue(supported.contains(phase),
                "handle() was called at unsupported phase: " + phase);
        }
    }

    /**
     * Verify that a hook declaring a WRITE phase is not called during a READ parse.
     */
    @Test
    void property2_writeOnlyHook_notCalledDuringRead() {
        // WriteOnlyHook supports WRITE_AFTER_GET only; StringHook handles the read.
        FixedParser parser = FixedParser.parser().use(new WriteOnlyModule());
        parser.parse(SimpleModel.class, "hello     ");

        // WriteOnlyHook must not have been called during a read parse
        assertTrue(writeOnlyHookCalls.isEmpty(),
            "Write-only hook must not be called during read; called at: " + writeOnlyHookCalls);
    }
}
