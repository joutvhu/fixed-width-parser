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
 * Tests that dependent fields are parsed in the correct order,
 * circular dependency is detected, and @FixedConditional works.
 * All these tests will FAIL until Phase 4 is implemented.
 */
class FieldDependencyTest {

    // -------------------------------------------------------------------------
    // Models
    // -------------------------------------------------------------------------

    /**
     * B depends on A — B must be parsed after A even if B comes before it in the class
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class DependencyModel {
        // B is declared before A in the class, but depends on A
        @FixedConditional(dependsOnField = "typeCode", whenValue = "X")
        @FixedField(start = 1, length = 4)
        private String dataX; // only present when typeCode = "X"

        @FixedField(start = 0, length = 1)
        private String typeCode; // A — no dependency
    }

    /**
     * Circular dependency — must throw at build time
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class CircularModel {
        @FixedConditional(dependsOnField = "fieldB", whenValue = "1")
        @FixedField(length = 1)
        private String fieldA; // A depends on B

        @FixedConditional(dependsOnField = "fieldA", whenValue = "1")
        @FixedField(start = 1, length = 1)
        private String fieldB; // B depends on A → circular!
    }

    /**
     * Conditional field — skipped when condition is not met
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    public static class ConditionalModel {
        @FixedField(start = 0, length = 1)
        private String type;

        @FixedConditional(dependsOnField = "type", whenValue = "A")
        @FixedField(start = 1, length = 5)
        private String dataA; // only parsed when type = "A"

        @FixedConditional(dependsOnField = "type", whenValue = "B")
        @FixedField(start = 1, length = 5)
        private String dataB; // only parsed when type = "B"
    }

    /**
     * Multiple levels of dependency: C → B → A
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
    // Dependent fields — parsed in the correct order
    // -------------------------------------------------------------------------

    @Test
    void dependentField_parsedAfterDependency() {
        // typeCode = "X" → dataX is parsed
        DependencyModel model = FixedParser.parser()
            .parse(DependencyModel.class, "Xhello");

        assertEquals("X", model.getTypeCode());
        assertEquals("hell", model.getDataX());
    }

    @Test
    void dependentField_skippedWhenConditionNotMet() {
        // typeCode = "Y" → dataX is skipped (null)
        DependencyModel model = FixedParser.parser()
            .parse(DependencyModel.class, "Yhello");

        assertEquals("Y", model.getTypeCode());
        assertNull(model.getDataX());
    }

    // -------------------------------------------------------------------------
    // @FixedConditional — field is skipped when condition is not met
    // -------------------------------------------------------------------------

    @Test
    void conditionalField_typeA_parsesDataA() {
        ConditionalModel model = FixedParser.parser()
            .parse(ConditionalModel.class, "Ahello");

        assertEquals("A", model.getType());
        assertEquals("hello", model.getDataA());
        assertNull(model.getDataB()); // dataB is skipped
    }

    @Test
    void conditionalField_typeB_parsesDataB() {
        ConditionalModel model = FixedParser.parser()
            .parse(ConditionalModel.class, "Bworld");

        assertEquals("B", model.getType());
        assertNull(model.getDataA()); // dataA is skipped
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
        assertNull(model.getFieldB()); // B skipped because A != "OK"
        assertNull(model.getFieldC()); // C skipped because B is null
    }

    // -------------------------------------------------------------------------
    // Circular dependency detection
    // -------------------------------------------------------------------------

    @Test
    void circularDependency_throwsAtBuildTime() {
        // CircularDependencyException must be thrown when building FixedTypeInfo
        // (at the first parse, not at runtime)
        assertThrows(CircularDependencyException.class,
            () -> FixedParser.parser().parse(CircularModel.class, "AB"));
    }

    // -------------------------------------------------------------------------
    // Export with conditional field
    // -------------------------------------------------------------------------

    @Test
    void conditionalField_exportTypeA_writesDataA() {
        ConditionalModel model = new ConditionalModel();
        model.setType("A");
        model.setDataA("hello");
        model.setDataB(null); // no dataB

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
