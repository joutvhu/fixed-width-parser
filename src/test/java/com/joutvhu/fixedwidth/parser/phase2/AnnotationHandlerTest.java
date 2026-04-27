package com.joutvhu.fixedwidth.parser.phase2;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.convert.AnnotationHandler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
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
 * Kiểm tra handler được gọi đúng phase, ctx.getPhase() đúng,
 * và custom handler hoạt động không cần đăng ký module.
 * Tất cả test này sẽ FAIL cho đến khi Phase 2 được implement.
 */
class AnnotationHandlerTest {

    // -------------------------------------------------------------------------
    // Custom annotation + handler definitions (dùng trong test)
    // -------------------------------------------------------------------------

    static List<Phase> CALLED_PHASES = new ArrayList<>();
    static List<String> CALLED_VALUES = new ArrayList<>();

    public static class TrackingHandler implements AnnotationHandler<TrackAnnotation> {
        @Override
        public Set<Phase> getPhases(TrackAnnotation annotation) {
            return Set.of(Phase.READ_AFTER_CUT, Phase.READ_AFTER_TRANSFORM);
        }

        @Override
        public void handle(TrackAnnotation ann, FixedTypeInfo info, ParseContext ctx) {
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
    // Handler được gọi đúng phase
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

        // TrackingHandler chỉ đăng ký READ_AFTER_CUT và READ_AFTER_TRANSFORM
        assertFalse(CALLED_PHASES.contains(Phase.READ_AFTER_CONVERT));
        assertFalse(CALLED_PHASES.contains(Phase.WRITE_AFTER_GET));
    }

    @Test
    void ctxGetPhaseReturnsCorrectPhaseInHandle() {
        CALLED_PHASES.clear();
        FixedParser.parser().parse(TrackedModel.class, "hello");

        // Mỗi lần handle() được gọi, ctx.getPhase() phải đúng với phase đó
        assertEquals(2, CALLED_PHASES.size());
        assertEquals(Phase.READ_AFTER_CUT, CALLED_PHASES.get(0));
        assertEquals(Phase.READ_AFTER_TRANSFORM, CALLED_PHASES.get(1));
    }

    // -------------------------------------------------------------------------
    // Custom handler không cần đăng ký module
    // -------------------------------------------------------------------------

    @Test
    void customHandlerWorksWithoutModuleRegistration() {
        // Chỉ cần đặt @FixedHandler trên annotation là đủ
        CALLED_PHASES.clear();
        FixedParser parser = FixedParser.parser(); // DefaultModule, không thêm gì

        parser.parse(TrackedModel.class, "hello");

        assertFalse(CALLED_PHASES.isEmpty()); // handler được gọi tự động
    }

    // -------------------------------------------------------------------------
    // Handler đăng ký nhiều phase — một handle() xử lý tất cả
    // -------------------------------------------------------------------------

    @Test
    void handlerRegisteredForMultiplePhasesCalledCorrectNumberOfTimes() {
        CALLED_PHASES.clear();
        FixedParser.parser().parse(TrackedModel.class, "hello");

        // TrackingHandler đăng ký 2 phase → được gọi 2 lần cho 1 field
        assertEquals(2, CALLED_PHASES.size());
    }

    // -------------------------------------------------------------------------
    // Handler trên WRITE direction
    // -------------------------------------------------------------------------

    static List<Phase> WRITE_PHASES = new ArrayList<>();

    public static class WriteTrackingHandler implements AnnotationHandler<WriteTrackAnnotation> {
        @Override
        public Set<Phase> getPhases(WriteTrackAnnotation annotation) {
            return Set.of(Phase.WRITE_AFTER_GET, Phase.WRITE_AFTER_TRANSFORM);
        }

        @Override
        public void handle(WriteTrackAnnotation ann, FixedTypeInfo info, ParseContext ctx) {
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
    // Handler tham gia cả READ và WRITE
    // -------------------------------------------------------------------------

    static List<Phase> BOTH_PHASES = new ArrayList<>();

    public static class BothDirectionHandler implements AnnotationHandler<BothAnnotation> {
        @Override
        public Set<Phase> getPhases(BothAnnotation annotation) {
            return Set.of(Phase.READ_AFTER_CUT, Phase.WRITE_AFTER_GET);
        }

        @Override
        public void handle(BothAnnotation ann, FixedTypeInfo info, ParseContext ctx) {
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
    // Handler có thể modify giá trị qua context
    // -------------------------------------------------------------------------

    public static class UpperCaseHandler implements AnnotationHandler<UpperCase> {
        @Override
        public Set<Phase> getPhases(UpperCase annotation) {
            return Set.of(Phase.READ_AFTER_TRANSFORM);
        }

        @Override
        public void handle(UpperCase ann, FixedTypeInfo info, ParseContext ctx) {
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
    // getDependencies() — handler khai báo dependency
    // -------------------------------------------------------------------------

    public static class DependencyAwareHandler implements AnnotationHandler<DependsOnField> {
        @Override
        public Set<String> getDependencies(DependsOnField annotation, FixedTypeInfo info) {
            return Set.of(annotation.field());
        }

        @Override
        public Set<Phase> getPhases(DependsOnField annotation) {
            return Set.of(Phase.READ_AFTER_CONVERT);
        }

        @Override
        public void handle(DependsOnField ann, FixedTypeInfo info, ParseContext ctx) {
            // no-op — chỉ test getDependencies()
        }
    }

    @FixedHandler(DependencyAwareHandler.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface DependsOnField {
        String field();
    }

    @Test
    void getDependenciesReturnsCorrectFields() {
        DependsOnField ann = new DependsOnField() {
            @Override
            public String field() {
                return "otherField";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return DependsOnField.class;
            }
        };

        DependencyAwareHandler handler = new DependencyAwareHandler();
        Set<String> deps = handler.getDependencies(ann, null);

        assertEquals(Set.of("otherField"), deps);
    }

    @Test
    void handlerWithNoDependenciesReturnsEmptySet() {
        TrackingHandler handler = new TrackingHandler();
        TrackAnnotation ann = new TrackAnnotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TrackAnnotation.class;
            }
        };

        assertEquals(Collections.emptySet(), handler.getDependencies(ann, null));
    }
}
