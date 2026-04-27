package com.joutvhu.fixedwidth.parser.phase7;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.convert.hook.ObjectHook;
import com.joutvhu.fixedwidth.parser.convert.hook.StringHook;
import com.joutvhu.fixedwidth.parser.exception.NoHookFoundException;
import com.joutvhu.fixedwidth.parser.module.FixedModule;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Task 10.1 — Property 4: Annotation-hook independence.
 *
 * <p>FOR ALL annotation-hook H activated by annotation A, H is called regardless
 * of whether any module-hook supports() = true for the same field.
 *
 * <p><b>Validates: Requirements 3.1, 4.2, 10.4</b>
 */
class AnnotationHookIndependenceTest {

    // ── Tracking annotation-hook ──────────────────────────────────────────────

    static final AtomicInteger annotationHookCallCount = new AtomicInteger(0);

    public static class TrackingAnnotationHook implements Hook {

        public TrackingAnnotationHook() {
        }

        @Override
        public Set<Phase> phases() {
            return EnumSet.of(Phase.READ_PRE_CUT);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            annotationHookCallCount.incrementAndGet();
        }
    }

    // ── Custom annotation backed by TrackingAnnotationHook ────────────────────

    @FixedHandler(TrackingAnnotationHook.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Tracked {
    }

    // ── Models ────────────────────────────────────────────────────────────────

    /**
     * Model whose field has @Tracked — used with a module that supports String.
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class TrackedModel {
        @Tracked
        @FixedField(length = 10)
        String value;
    }

    // ── Modules ───────────────────────────────────────────────────────────────

    /**
     * Module that supports String fields and objects (normal case).
     * ObjectHook is required to handle the @FixedObject model itself.
     */
    public static class StringModule extends FixedModule {
        public StringModule() {
            super();
            registerHook(StringHook.class);
            registerHook(ObjectHook.class);
        }
    }

    /**
     * Module that supports objects but NOT String fields.
     * When the parser reaches the String field, no module-hook supports it
     * → NoHookFoundException is thrown after the annotation-hook runs.
     */
    public static class ObjectOnlyModule extends FixedModule {
        public ObjectOnlyModule() {
            super();
            registerHook(ObjectHook.class);
        }
    }

    @BeforeEach
    void setUp() {
        annotationHookCallCount.set(0);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * Case (a): A module-hook supports the field.
     * The annotation-hook must still be called.
     */
    @Test
    void property4_annotationHookCalled_whenModuleHookSupports() {
        FixedParser parser = FixedParser.parser().use(new StringModule());
        TrackedModel result = parser.parse(TrackedModel.class, "hello     ");

        assertNotNull(result);
        assertEquals(1, annotationHookCallCount.get(),
            "Annotation-hook must be called exactly once even when a module-hook supports the field");
    }

    /**
     * Case (b): No module-hook supports the String field (only ObjectHook is registered).
     * The annotation-hook must still be called before NoHookFoundException is thrown.
     */
    @Test
    void property4_annotationHookCalled_beforeNoHookFoundException() {
        FixedParser parser = FixedParser.parser().use(new ObjectOnlyModule());

        assertThrows(NoHookFoundException.class,
            () -> parser.parse(TrackedModel.class, "hello     "),
            "NoHookFoundException must be thrown when no module-hook supports the String field");

        assertEquals(1, annotationHookCallCount.get(),
            "Annotation-hook must be called before NoHookFoundException is thrown");
    }
}
