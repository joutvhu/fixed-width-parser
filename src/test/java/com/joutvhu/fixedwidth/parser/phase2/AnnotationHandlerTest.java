package com.joutvhu.fixedwidth.parser.phase2;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 2 — AnnotationHandler dispatch
 * <p>
 * Tests that the handler is called in the correct phase, ctx.getPhase() is correct,
 * and custom handlers work without module registration.
 * All these tests will FAIL until Phase 2 is implemented.
 */
class AnnotationHandlerTest {

    // -------------------------------------------------------------------------
    // Custom annotation + handler definitions (used in tests)
    // -------------------------------------------------------------------------

    static List<Phase> CALLED_PHASES = new ArrayList<>();
    static List<String> CALLED_VALUES = new ArrayList<>();

    public static class TrackingHandler implements Hook {
        @Override
        public Set<Phase> getSupportedPhases() {
            return Set.of(Phase.READ_AFTER_CUT, Phase.READ_AFTER_TRANSFORM);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            CALLED_PHASES.add(ctx.getPhase());
            CALLED_VALUES.add(ctx.getRawString());
        }
    }

    @FixedHandler(TrackingHandler.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface TrackAnnotation {
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackedModel {
        @TrackAnnotation
        @FixedField(length = 5)
        private String value;
    }

    // -------------------------------------------------------------------------
    // Handler is called in the correct phase
    // -------------------------------------------------------------------------

    @Test
    void handlerCalledAtRegisteredPhases() {
        CALLED_PHASES.clear();
        FixedParser.parser().parse(TrackedModel.class, "hello");

        assertTrue(CALLED_PHASES.contains(Phase.READ_AFTER_CUT));
        assertTrue(CALLED_PHASES.contains(Phase.READ_AFTER_TRANSFORM));
    }

    @Test
    void handlerNotCalledAtUnregisteredPhases() {
        CALLED_PHASES.clear();
        FixedParser.parser().parse(TrackedModel.class, "hello");

        // TrackingHandler only registers READ_AFTER_CUT and READ_AFTER_TRANSFORM
        assertFalse(CALLED_PHASES.contains(Phase.READ_AFTER_CONVERT));
        assertFalse(CALLED_PHASES.contains(Phase.WRITE_AFTER_GET));
    }

    @Test
    void ctxGetPhaseReturnsCorrectPhaseInHandle() {
        CALLED_PHASES.clear();
        FixedParser.parser().parse(TrackedModel.class, "hello");

        // Each time handle() is called, ctx.getPhase() must match that phase
        assertEquals(2, CALLED_PHASES.size());
        assertEquals(Phase.READ_AFTER_CUT, CALLED_PHASES.get(0));
        assertEquals(Phase.READ_AFTER_TRANSFORM, CALLED_PHASES.get(1));
    }

    // -------------------------------------------------------------------------
    // Custom handler works without module registration
    // -------------------------------------------------------------------------

    @Test
    void customHandlerWorksWithoutModuleRegistration() {
        // Simply placing @FixedHandler on the annotation is enough
        CALLED_PHASES.clear();
        FixedParser parser = FixedParser.parser(); // DefaultModule, nothing else added

        parser.parse(TrackedModel.class, "hello");

        assertFalse(CALLED_PHASES.isEmpty()); // handler is called automatically
    }

    // -------------------------------------------------------------------------
    // Handler registered for multiple phases — one handle() processes all
    // -------------------------------------------------------------------------

    @Test
    void handlerRegisteredForMultiplePhasesCalledCorrectNumberOfTimes() {
        CALLED_PHASES.clear();
        FixedParser.parser().parse(TrackedModel.class, "hello");

        // TrackingHandler registers 2 phases → called twice for 1 field
        assertEquals(2, CALLED_PHASES.size());
    }

    // -------------------------------------------------------------------------
    // Handler on WRITE direction
    // -------------------------------------------------------------------------

    static List<Phase> WRITE_PHASES = new ArrayList<>();

    public static class WriteTrackingHandler implements Hook {
        @Override
        public Set<Phase> getSupportedPhases() {
            return Set.of(Phase.WRITE_AFTER_GET, Phase.WRITE_AFTER_TRANSFORM);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            WRITE_PHASES.add(ctx.getPhase());
        }
    }

    @FixedHandler(WriteTrackingHandler.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface WriteTrackAnnotation {
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WriteTrackedModel {
        @WriteTrackAnnotation
        @FixedField(length = 5)
        private String value;
    }

    @Test
    void handlerCalledAtWritePhases() {
        WRITE_PHASES.clear();
        FixedParser.parser().export(new WriteTrackedModel("hello"));

        assertTrue(WRITE_PHASES.contains(Phase.WRITE_AFTER_GET));
        assertTrue(WRITE_PHASES.contains(Phase.WRITE_AFTER_TRANSFORM));
    }

    @Test
    void handlerNotCalledAtReadPhasesWhenExporting() {
        WRITE_PHASES.clear();
        FixedParser.parser().export(new WriteTrackedModel("hello"));

        assertFalse(WRITE_PHASES.contains(Phase.READ_AFTER_CUT));
        assertFalse(WRITE_PHASES.contains(Phase.READ_AFTER_TRANSFORM));
    }

    // -------------------------------------------------------------------------
    // Handler participating in both READ and WRITE
    // -------------------------------------------------------------------------

    static List<Phase> BOTH_PHASES = new ArrayList<>();

    public static class BothDirectionHandler implements Hook {
        @Override
        public Set<Phase> getSupportedPhases() {
            return Set.of(Phase.READ_AFTER_CUT, Phase.WRITE_AFTER_GET);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            BOTH_PHASES.add(ctx.getPhase());
        }
    }

    @FixedHandler(BothDirectionHandler.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface BothAnnotation {
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BothModel {
        @BothAnnotation
        @FixedField(length = 5)
        private String value;
    }

    @Test
    void handlerCalledInBothReadAndWrite() {
        BOTH_PHASES.clear();

        FixedParser parser = FixedParser.parser();
        parser.parse(BothModel.class, "hello");
        parser.export(new BothModel("hello"));

        assertTrue(BOTH_PHASES.contains(Phase.READ_AFTER_CUT));
        assertTrue(BOTH_PHASES.contains(Phase.WRITE_AFTER_GET));
    }

    // -------------------------------------------------------------------------
    // Handler can modify values via context
    // -------------------------------------------------------------------------

    public static class UpperCaseHandler implements Hook {
        @Override
        public Set<Phase> getSupportedPhases() {
            return Set.of(Phase.READ_AFTER_TRANSFORM);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            String processed = ctx.getProcessedString();
            if (processed != null) {
                ctx.setProcessedString(processed.toUpperCase());
            }
        }
    }

    @FixedHandler(UpperCaseHandler.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface UpperCase {
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpperCaseModel {
        @UpperCase
        @FixedField(length = 5)
        private String value;
    }

    @Test
    void handlerCanModifyProcessedString() {
        UpperCaseModel model = FixedParser.parser().parse(UpperCaseModel.class, "hello");
        assertEquals("HELLO", model.getValue());
    }

    // -------------------------------------------------------------------------
    // getDependencies() — handler declares dependencies
    // -------------------------------------------------------------------------

    public static class DependencyAwareHandler implements Hook {
        @Override
        public Set<String> getDependencies(FixedTypeInfo info) {
            DependsOnField annotation = info.getAnnotation(DependsOnField.class);
            return Set.of(annotation.field());
        }

        @Override
        public Set<Phase> getSupportedPhases() {
            return Set.of(Phase.READ_AFTER_CONVERT);
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            // no-op — only testing getDependencies()
        }
    }

    @FixedHandler(DependencyAwareHandler.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface DependsOnField {
        String field();
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    static class DependsOnFieldModel {
        @FixedField(length = 5)
        String otherField;

        @DependsOnField(field = "otherField")
        @FixedField(start = 5, length = 5)
        String dependent;
    }

    @Test
    void getDependenciesReturnsCorrectFields() throws Exception {
        DependencyAwareHandler handler = new DependencyAwareHandler();
        FixedTypeInfo info = FixedTypeInfo.of(DependsOnFieldModel.class.getDeclaredField("dependent"));
        Set<String> deps = handler.getDependencies(info);
        assertEquals(Set.of("otherField"), deps);
    }

    @Test
    void handlerWithNoDependenciesReturnsEmptySet() throws Exception {
        TrackingHandler handler = new TrackingHandler();
        FixedTypeInfo info = FixedTypeInfo.of(TrackedModel.class.getDeclaredField("value"));
        assertEquals(Collections.emptySet(), handler.getDependencies(info));
    }
}
