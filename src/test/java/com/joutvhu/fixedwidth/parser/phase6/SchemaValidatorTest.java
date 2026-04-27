package com.joutvhu.fixedwidth.parser.phase6;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;
import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.validation.SchemaValidator;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6 — Schema validation
 */
class SchemaValidatorTest {

    // ── Models ────────────────────────────────────────────────────────────────

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class ValidModel {
        @FixedField(start = 0, length = 5)
        private String code;

        @FixedField(start = 5, length = 10)
        private String name;

        @FixedField(start = 15, length = 5)
        private Integer amount;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class OverlapModel {
        @FixedField(start = 0, length = 10)
        private String fieldA;

        @FixedField(start = 5, length = 10)  // overlaps fieldA (5..15 vs 0..10)
        private String fieldB;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class MissingConditionalDepModel {
        @FixedField(start = 0, length = 5)
        private String type;

        @FixedConditional(dependsOnField = "nonExistentField", whenValue = "A")
        @FixedField(start = 5, length = 10)
        private String data;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class MissingCountDepModel {
        @FixedCount(field = "nonExistentCount")
        @FixedField(start = 0, length = 0)
        private List<String> items;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class AdjacentModel {
        // Adjacent fields (not overlapping) — should be valid
        @FixedField(start = 0, length = 5)
        private String a;

        @FixedField(start = 5, length = 5)
        private String b;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class UnlimitedLengthModel {
        // length=0 means unlimited — should not be checked for overlap
        @FixedField(start = 0, length = 5)
        private String code;

        @FixedField(start = 5, length = 0)
        private String rest;
    }

    // ── Valid schema ──────────────────────────────────────────────────────────

    @Test
    void validModel_isValid() {
        SchemaValidator v = SchemaValidator.validate(ValidModel.class);
        assertTrue(v.isValid());
        assertTrue(v.getErrors().isEmpty());
    }

    @Test
    void validModel_throwIfInvalid_doesNotThrow() {
        assertDoesNotThrow(() ->
                SchemaValidator.validate(ValidModel.class).throwIfInvalid());
    }

    @Test
    void adjacentFields_notOverlap_isValid() {
        SchemaValidator v = SchemaValidator.validate(AdjacentModel.class);
        assertTrue(v.isValid());
    }

    @Test
    void unlimitedLengthField_skippedInOverlapCheck_isValid() {
        SchemaValidator v = SchemaValidator.validate(UnlimitedLengthModel.class);
        assertTrue(v.isValid());
    }

    // ── Field overlap ─────────────────────────────────────────────────────────

    @Test
    void overlappingFields_isInvalid() {
        SchemaValidator v = SchemaValidator.validate(OverlapModel.class);
        assertFalse(v.isValid());
    }

    @Test
    void overlappingFields_errorMentionsFieldNames() {
        SchemaValidator v = SchemaValidator.validate(OverlapModel.class);
        List<String> errors = v.getErrors();
        assertFalse(errors.isEmpty());
        String error = errors.get(0);
        assertTrue(error.contains("fieldA") || error.contains("fieldB"),
                "Error should mention field names: " + error);
    }

    @Test
    void overlappingFields_throwIfInvalid_throws() {
        assertThrows(FixedParserException.class,
                () -> SchemaValidator.validate(OverlapModel.class).throwIfInvalid());
    }

    // ── Missing dependency field ──────────────────────────────────────────────

    @Test
    void missingConditionalDep_isInvalid() {
        SchemaValidator v = SchemaValidator.validate(MissingConditionalDepModel.class);
        assertFalse(v.isValid());
    }

    @Test
    void missingConditionalDep_errorMentionsFieldName() {
        SchemaValidator v = SchemaValidator.validate(MissingConditionalDepModel.class);
        String error = v.getErrors().get(0);
        assertTrue(error.contains("nonExistentField"), "Error: " + error);
    }

    @Test
    void missingCountDep_isInvalid() {
        SchemaValidator v = SchemaValidator.validate(MissingCountDepModel.class);
        assertFalse(v.isValid());
    }

    @Test
    void missingCountDep_errorMentionsFieldName() {
        SchemaValidator v = SchemaValidator.validate(MissingCountDepModel.class);
        String error = v.getErrors().get(0);
        assertTrue(error.contains("nonExistentCount"), "Error: " + error);
    }

    // ── FixedParser integration ───────────────────────────────────────────────

    @Test
    void fixedParser_validate_returnsSchemaValidator() {
        SchemaValidator v = FixedParser.parser().validate(ValidModel.class);
        assertNotNull(v);
        assertTrue(v.isValid());
    }

    @Test
    void fixedParser_validate_invalidModel_throws() {
        assertThrows(FixedParserException.class,
                () -> FixedParser.parser().validate(OverlapModel.class).throwIfInvalid());
    }
}
