package com.joutvhu.fixedwidth.parser.phase3;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedPadding;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Phase 3 — Annotation composition (B2)
 * <p>
 * Tests that composed annotations (meta-annotations) are correctly unwrapped.
 * All these tests will FAIL until Phase 3 is implemented.
 */
class AnnotationCompositionTest {

    // -------------------------------------------------------------------------
    // Composed annotations (preset)
    // -------------------------------------------------------------------------

    /**
     * Preset: right-aligned, zero-padded integer
     */
    @FixedPadding(value = '0', alignment = Alignment.RIGHT)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface ZeroPaddedNumber {
    }

    /**
     * Preset: date with standard format
     */
    @FixedFormat(format = "yyyy-MM-dd")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface StandardDate {
    }

    // -------------------------------------------------------------------------
    // Models using composed annotations
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComposedModel {
        @ZeroPaddedNumber
        @FixedField(length = 6)
        private Integer count;

        @ZeroPaddedNumber
        @FixedField(start = 6, length = 8)
        private Long amount;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComposedDateModel {
        @StandardDate
        @FixedField(length = 10)
        private java.time.LocalDate date;
    }

    // -------------------------------------------------------------------------
    // Composed annotation is correctly unwrapped
    // -------------------------------------------------------------------------

    @Test
    void composedAnnotation_paddingApplied() {
        ComposedModel model = new ComposedModel(42, 1000L);
        String exported = FixedParser.parser().export(model);

        assertEquals("000042", exported.substring(0, 6));   // zero-padded, right-aligned
        assertEquals("00001000", exported.substring(6, 14));
    }

    @Test
    void composedAnnotation_parsedCorrectly() {
        ComposedModel model = FixedParser.parser()
            .parse(ComposedModel.class, "00004200001000");

        assertEquals(42, model.getCount());
        assertEquals(1000L, model.getAmount());
    }

    @Test
    void composedDateAnnotation_formatApplied() {
        ComposedDateModel model = FixedParser.parser()
            .parse(ComposedDateModel.class, "2024-01-15");

        assertEquals(java.time.LocalDate.of(2024, 1, 15), model.getDate());
    }

    @Test
    void composedDateAnnotation_exportFormatted() {
        ComposedDateModel model = new ComposedDateModel(java.time.LocalDate.of(2024, 1, 15));
        assertEquals("2024-01-15", FixedParser.parser().export(model));
    }

    // -------------------------------------------------------------------------
    // Multiple fields using the same composed annotation — no shared state
    // -------------------------------------------------------------------------

    @Test
    void multipleFieldsWithSameComposedAnnotation_noSharedState() {
        ComposedModel m1 = new ComposedModel(1, 2L);
        ComposedModel m2 = new ComposedModel(999, 888L);

        String e1 = FixedParser.parser().export(m1);
        String e2 = FixedParser.parser().export(m2);

        assertEquals("000001", e1.substring(0, 6));
        assertEquals("000999", e2.substring(0, 6));
    }

    // -------------------------------------------------------------------------
    // Round-trip with composed annotation
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_composedAnnotation() {
        ComposedModel original = new ComposedModel(12345, 9876543L);
        String exported = FixedParser.parser().export(original);
        ComposedModel parsed = FixedParser.parser().parse(ComposedModel.class, exported);

        assertEquals(original.getCount(), parsed.getCount());
        assertEquals(original.getAmount(), parsed.getAmount());
    }
}
