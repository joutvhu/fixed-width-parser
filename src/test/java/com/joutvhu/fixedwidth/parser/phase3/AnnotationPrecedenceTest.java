package com.joutvhu.fixedwidth.parser.phase3;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedPadding;
import com.joutvhu.fixedwidth.parser.annotation.FixedRequired;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.exception.MandatoryValueException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3 — Annotation precedence
 *
 * Kiểm tra thứ tự ưu tiên: field annotation > class annotation > default.
 * Tất cả test này sẽ FAIL cho đến khi Phase 3 được implement.
 */
class AnnotationPrecedenceTest {

    // -------------------------------------------------------------------------
    // Models
    // -------------------------------------------------------------------------

    // Class-level padding: tất cả field dùng '*' padding
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
        private String fieldA; // dùng class-level '*'

        @FixedPadding(value = '-', alignment = Alignment.RIGHT) // override
        @FixedField(start = 8, length = 8)
        private String fieldB; // dùng field-level '-'
    }

    // Backward compat: @FixedField(padding) vẫn hoạt động
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegacyPaddingModel {
        @FixedField(length = 8, padding = '#')
        private String value;
    }

    // @FixedRequired annotation riêng
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredAnnotationModel {
        @FixedRequired
        @FixedField(length = 5)
        private String value;
    }

    // Backward compat: @FixedField(required=true) vẫn hoạt động
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegacyRequiredModel {
        @FixedField(length = 5, required = true)
        private String value;
    }

    // -------------------------------------------------------------------------
    // Class-level annotation áp dụng cho tất cả field
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
    // Backward compatibility: @FixedField(padding) vẫn hoạt động
    // -------------------------------------------------------------------------

    @Test
    void legacyPaddingAttribute_stillWorks() {
        LegacyPaddingModel model = new LegacyPaddingModel("hi");
        String exported = FixedParser.parser().export(model);
        assertEquals("hi######", exported);
    }

    @Test
    void legacyPaddingAttribute_parsedCorrectly() {
        LegacyPaddingModel model = FixedParser.parser()
                .parse(LegacyPaddingModel.class, "hi######");
        assertEquals("hi", model.getValue());
    }

    // -------------------------------------------------------------------------
    // @FixedRequired annotation riêng
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
    // Backward compat: @FixedField(required=true) vẫn hoạt động
    // -------------------------------------------------------------------------

    @Test
    void legacyRequiredAttribute_nullExport_throws() {
        assertThrows(MandatoryValueException.class,
                () -> FixedParser.parser().export(new LegacyRequiredModel(null)));
    }

    // -------------------------------------------------------------------------
    // Precedence: field annotation > class annotation
    // -------------------------------------------------------------------------

    @Test
    void fieldAnnotationHasHigherPrecedenceThanClassAnnotation() {
        // fieldB dùng '-' (field-level) thay vì '*' (class-level)
        FieldOverrideModel model = FixedParser.parser()
                .parse(FieldOverrideModel.class, "hello***---world");

        assertEquals("hello", model.getFieldA()); // stripped '*'
        assertEquals("world", model.getFieldB()); // stripped '-'
    }
}
