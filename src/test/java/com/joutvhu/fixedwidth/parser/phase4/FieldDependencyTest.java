package com.joutvhu.fixedwidth.parser.phase4;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.exception.CircularDependencyException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Phase 4 — Field dependency resolution
 * <p>
 * Kiểm tra field phụ thuộc nhau được parse đúng thứ tự,
 * circular dependency được phát hiện, và @FixedConditional hoạt động.
 * Tất cả test này sẽ FAIL cho đến khi Phase 4 được implement.
 */
class FieldDependencyTest {

    // -------------------------------------------------------------------------
    // Models
    // -------------------------------------------------------------------------

    /**
     * B phụ thuộc A — B phải parse sau A dù B đứng trước trong class
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class DependencyModel {
        // B khai báo trước A trong class, nhưng phụ thuộc vào A
        @FixedConditional(dependsOnField = "typeCode", whenValue = "X")
        @FixedField(start = 1, length = 4)
        private String dataX; // chỉ có khi typeCode = "X"

        @FixedField(start = 0, length = 1)
        private String typeCode; // A — không có dependency
    }

    /**
     * Circular dependency — phải throw tại build time
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class CircularModel {
        @FixedConditional(dependsOnField = "fieldB", whenValue = "1")
        @FixedField(length = 1)
        private String fieldA; // A phụ thuộc B

        @FixedConditional(dependsOnField = "fieldA", whenValue = "1")
        @FixedField(start = 1, length = 1)
        private String fieldB; // B phụ thuộc A → circular!
    }

    /**
     * Conditional field — bị skip khi điều kiện không thỏa
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class ConditionalModel {
        @FixedField(start = 0, length = 1)
        private String type;

        @FixedConditional(dependsOnField = "type", whenValue = "A")
        @FixedField(start = 1, length = 5)
        private String dataA; // chỉ parse khi type = "A"

        @FixedConditional(dependsOnField = "type", whenValue = "B")
        @FixedField(start = 1, length = 5)
        private String dataB; // chỉ parse khi type = "B"
    }

    /**
     * Nhiều tầng dependency: C → B → A
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class ChainDependencyModel {
        @FixedField(start = 0, length = 2)
        private String fieldA;

        @FixedConditional(dependsOnField = "fieldA", whenValue = "OK")
        @FixedField(start = 2, length = 2)
        private String fieldB;

        @FixedConditional(dependsOnField = "fieldB", whenValue = "GO")
        @FixedField(start = 4, length = 2)
        private String fieldC;
    }

    // -------------------------------------------------------------------------
    // Field phụ thuộc nhau — parse đúng thứ tự
    // -------------------------------------------------------------------------

    @Test
    void dependentField_parsedAfterDependency() {
        // typeCode = "X" → dataX được parse
        DependencyModel model = FixedParser.parser()
            .parse(DependencyModel.class, "Xhello");

        assertEquals("X", model.getTypeCode());
        assertEquals("hell", model.getDataX());
    }

    @Test
    void dependentField_skippedWhenConditionNotMet() {
        // typeCode = "Y" → dataX bị skip (null)
        DependencyModel model = FixedParser.parser()
            .parse(DependencyModel.class, "Yhello");

        assertEquals("Y", model.getTypeCode());
        assertNull(model.getDataX());
    }

    // -------------------------------------------------------------------------
    // @FixedConditional — field bị skip khi điều kiện không thỏa
    // -------------------------------------------------------------------------

    @Test
    void conditionalField_typeA_parsesDataA() {
        ConditionalModel model = FixedParser.parser()
            .parse(ConditionalModel.class, "Ahello");

        assertEquals("A", model.getType());
        assertEquals("hello", model.getDataA());
        assertNull(model.getDataB()); // dataB bị skip
    }

    @Test
    void conditionalField_typeB_parsesDataB() {
        ConditionalModel model = FixedParser.parser()
            .parse(ConditionalModel.class, "Bworld");

        assertEquals("B", model.getType());
        assertNull(model.getDataA()); // dataA bị skip
        assertEquals("world", model.getDataB());
    }

    @Test
    void conditionalField_unknownType_bothSkipped() {
        ConditionalModel model = FixedParser.parser()
            .parse(ConditionalModel.class, "Cxxxxx");

        assertEquals("C", model.getType());
        assertNull(model.getDataA());
        assertNull(model.getDataB());
    }

    // -------------------------------------------------------------------------
    // Chain dependency: C → B → A
    // -------------------------------------------------------------------------

    @Test
    void chainDependency_allConditionsMet_allFieldsParsed() {
        ChainDependencyModel model = FixedParser.parser()
            .parse(ChainDependencyModel.class, "OKGOUP");

        assertEquals("OK", model.getFieldA());
        assertEquals("GO", model.getFieldB());
        assertEquals("UP", model.getFieldC());
    }

    @Test
    void chainDependency_firstConditionFails_restSkipped() {
        ChainDependencyModel model = FixedParser.parser()
            .parse(ChainDependencyModel.class, "NOGO  ");

        assertEquals("NO", model.getFieldA());
        assertNull(model.getFieldB()); // B skip vì A != "OK"
        assertNull(model.getFieldC()); // C skip vì B null
    }

    // -------------------------------------------------------------------------
    // Circular dependency detection
    // -------------------------------------------------------------------------

    @Test
    void circularDependency_throwsAtBuildTime() {
        // CircularDependencyException phải throw khi build FixedTypeInfo
        // (tại lần đầu tiên parse, không phải lúc runtime)
        assertThrows(CircularDependencyException.class,
            () -> FixedParser.parser().parse(CircularModel.class, "AB"));
    }

    // -------------------------------------------------------------------------
    // Export với conditional field
    // -------------------------------------------------------------------------

    @Test
    void conditionalField_exportTypeA_writesDataA() {
        ConditionalModel model = new ConditionalModel();
        model.setType("A");
        model.setDataA("hello");
        model.setDataB(null); // không có dataB

        String exported = FixedParser.parser().export(model);
        assertEquals("A", exported.substring(0, 1));
        assertEquals("hello", exported.substring(1, 6));
    }

    @Test
    void conditionalField_exportTypeB_writesDataB() {
        ConditionalModel model = new ConditionalModel();
        model.setType("B");
        model.setDataA(null);
        model.setDataB("world");

        String exported = FixedParser.parser().export(model);
        assertEquals("B", exported.substring(0, 1));
        assertEquals("world", exported.substring(1, 6));
    }
}
