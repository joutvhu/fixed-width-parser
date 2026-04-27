package com.joutvhu.fixedwidth.parser.phase3;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedPadding;
import com.joutvhu.fixedwidth.parser.annotation.FixedRequired;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.exception.MandatoryValueException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Phase 3 — Annotation precedence
 * <p>
 * Tests precedence: field annotation > class annotation > default.
 * All these tests will FAIL until Phase 3 is implemented.
 */
class AnnotationPrecedenceTest {

    // -------------------------------------------------------------------------
    // Models
    // -------------------------------------------------------------------------

    // Class-level padding: all fields use '*' padding
    @FixedObject
    @FixedPadding(value = '*', alignment = Alignment.LEFT)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassLevelPaddingModel {
        @FixedField(length = 8)
        private String fieldA;

        @FixedField(start = 8, length = 8)
        private String fieldB;
    }

    // Field override class-level padding
    @FixedObject
    @FixedPadding(value = '*', alignment = Alignment.LEFT)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldOverrideModel {
        @FixedField(length = 8)
        private String fieldA; // uses class-level '*'

        @FixedPadding(value = '-', alignment = Alignment.RIGHT) // override
        @FixedField(start = 8, length = 8)
        private String fieldB; // uses field-level '-'
    }

    // Backward compatibility: @FixedPadding on field still works
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegacyPaddingModel {
        @FixedPadding(value = '#')
        @FixedField(length = 8)
        private String value;
    }

    // Dedicated @FixedRequired annotation
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredAnnotationModel {
        @FixedRequired
        @FixedField(length = 5)
        private String value;
    }

    // Dedicated @FixedRequired annotation on field
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegacyRequiredModel {
        @FixedRequired
        @FixedField(length = 5)
        private String value;
    }

    // -------------------------------------------------------------------------
    // Class-level annotation applied to all fields
    // -------------------------------------------------------------------------

    @Test
    void classLevelPadding_appliedToAllFields() {
        ClassLevelPaddingModel model = new ClassLevelPaddingModel("hello", "world");
        String exported = FixedParser.parser().export(model);

        assertEquals("hello***", exported.substring(0, 8));
        assertEquals("world***", exported.substring(8, 16));
    }

    @Test
    void classLevelPadding_strippedOnParse() {
        ClassLevelPaddingModel model = FixedParser.parser()
            .parse(ClassLevelPaddingModel.class, "hello***world***");

        assertEquals("hello", model.getFieldA());
        assertEquals("world", model.getFieldB());
    }

    // -------------------------------------------------------------------------
    // Field annotation override class annotation
    // -------------------------------------------------------------------------

    @Test
    void fieldAnnotation_overridesClassAnnotation() {
        FieldOverrideModel model = new FieldOverrideModel("hello", "world");
        String exported = FixedParser.parser().export(model);

        assertEquals("hello***", exported.substring(0, 8));  // class-level '*'
        assertEquals("---world", exported.substring(8, 16)); // field-level '-', RIGHT align
    }

    // -------------------------------------------------------------------------
    // Dedicated @FixedPadding annotation on field
    // -------------------------------------------------------------------------

    @Test
    void fixedPaddingAttribute_stillWorks() {
        LegacyPaddingModel model = new LegacyPaddingModel("hi");
        String exported = FixedParser.parser().export(model);
        assertEquals("hi######", exported);
    }

    @Test
    void fixedPaddingAttribute_parsedCorrectly() {
        LegacyPaddingModel model = FixedParser.parser()
            .parse(LegacyPaddingModel.class, "hi######");
        assertEquals("hi", model.getValue());
    }

    // -------------------------------------------------------------------------
    // @FixedRequired annotation
    // -------------------------------------------------------------------------

    @Test
    void fixedRequired_nullExport_throws() {
        assertThrows(MandatoryValueException.class,
            () -> FixedParser.parser().export(new RequiredAnnotationModel(null)));
    }

    @Test
    void fixedRequired_validValue_passes() {
        assertDoesNotThrow(
            () -> FixedParser.parser().export(new RequiredAnnotationModel("hello")));
    }

    // -------------------------------------------------------------------------
    // Dedicated @FixedRequired annotation on field
    // -------------------------------------------------------------------------

    @Test
    void fixedRequired_nullExport_throws_v2() {
        assertThrows(MandatoryValueException.class,
            () -> FixedParser.parser().export(new LegacyRequiredModel(null)));
    }

    // -------------------------------------------------------------------------
    // Precedence: field annotation > class annotation
    // -------------------------------------------------------------------------

    @Test
    void fieldAnnotationHasHigherPrecedenceThanClassAnnotation() {
        // fieldB uses '-' (field-level) instead of '*' (class-level)
        FieldOverrideModel model = FixedParser.parser()
            .parse(FieldOverrideModel.class, "hello***---world");

        assertEquals("hello", model.getFieldA()); // stripped '*'
        assertEquals("world", model.getFieldB()); // stripped '-'
    }
}
