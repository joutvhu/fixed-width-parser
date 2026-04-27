package com.joutvhu.fixedwidth.parser.phase4;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.support.BuiltPart;
import com.joutvhu.fixedwidth.parser.support.FixedStringBuilder;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 4 — FixedStringBuilder
 * <p>
 * Tests that the builder holds parts and joins correctly during write.
 * All these tests will FAIL until Phase 4 is implemented.
 */
class FixedStringBuilderTest {

    // -------------------------------------------------------------------------
    // Checksum handler — uses builder to compute checksum from already written fields
    // -------------------------------------------------------------------------

    public static class ChecksumHandler implements Hook {
        @Override
        public Set<Phase> phases() {
            return Set.of(Phase.WRITE_PRE_GET);
        }

        @Override
        public Set<String> dependencies(FixedTypeInfo info) {
            FixedChecksum annotation = info.getAnnotation(FixedChecksum.class);
            return Set.of(annotation.includeFields());
        }

        @Override
        public void handle(FixedTypeInfo info, ParseContext ctx) {
            FixedChecksum ann = info.getAnnotation(FixedChecksum.class);
            FixedStringBuilder builder = ctx.parentFrame().getBuilder();
            int checksum = 0;
            for (String fieldName : ann.includeFields()) {
                String part = builder.getPart(fieldName);
                if (part != null) {
                    for (char c : part.toCharArray()) checksum += c;
                }
            }
            ctx.setCurrentValue(checksum % 100); // 2-digit checksum
        }
    }

    @FixedHandler(ChecksumHandler.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface FixedChecksum {
        String[] includeFields();
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class ChecksumModel {
        @FixedField(start = 0, length = 5)
        private String code;

        @FixedField(start = 5, length = 10)
        private String data;

        @FixedChecksum(includeFields = {"code", "data"})
        @FixedField(start = 15, length = 2)
        private Integer checksum; // automatically computed from code + data
    }

    // -------------------------------------------------------------------------
    // Builder API directly
    // -------------------------------------------------------------------------

    @Test
    void builder_addAndGetPart() {
        FixedStringBuilder builder = FixedStringBuilder.create();
        builder.addPart("field1", null, "hello");
        builder.addPart("field2", null, "world");

        assertEquals("hello", builder.getPart("field1"));
        assertEquals("world", builder.getPart("field2"));
    }

    @Test
    void builder_hasPart() {
        FixedStringBuilder builder = FixedStringBuilder.create();
        builder.addPart("field1", null, "hello");

        assertTrue(builder.hasPart("field1"));
        assertFalse(builder.hasPart("field2"));
    }

    @Test
    void builder_getAllParts_inOrderAdded() {
        FixedStringBuilder builder = FixedStringBuilder.create();
        builder.addPart("a", null, "1");
        builder.addPart("b", null, "2");
        builder.addPart("c", null, "3");

        List<BuiltPart> parts = builder.getAllParts();
        assertEquals(3, parts.size());
        assertEquals("a", parts.get(0).getFieldName());
        assertEquals("b", parts.get(1).getFieldName());
        assertEquals("c", parts.get(2).getFieldName());
    }

    @Test
    void builder_replacePart() {
        FixedStringBuilder builder = FixedStringBuilder.create();
        builder.addPart("field1", null, "hello");
        builder.replacePart("field1", "world");

        assertEquals("world", builder.getPart("field1"));
    }

    @Test
    void builder_getCompletedParts_onlyAddedParts() {
        FixedStringBuilder builder = FixedStringBuilder.create();
        builder.addPart("a", null, "1");
        builder.addPart("b", null, "2");

        List<BuiltPart> completed = builder.getCompletedParts();
        assertEquals(2, completed.size());
    }

    // -------------------------------------------------------------------------
    // Builder in write pipeline — hook reads written parts
    // -------------------------------------------------------------------------

    @Test
    void checksumHandler_computesFromCompletedParts() {
        ChecksumModel model = new ChecksumModel();
        model.setCode("HELLO");
        model.setData("WORLD     ");
        // checksum will be computed automatically

        String exported = FixedParser.parser().export(model);

        assertNotNull(exported);
        assertEquals(17, exported.length()); // 5 + 10 + 2
        // Checksum field is not null
        String checksumStr = exported.substring(15, 17);
        assertFalse(checksumStr.isBlank());
    }

    @Test
    void checksumHandler_sameInputProducesSameChecksum() {
        ChecksumModel m1 = new ChecksumModel();
        m1.setCode("HELLO");
        m1.setData("WORLD     ");

        ChecksumModel m2 = new ChecksumModel();
        m2.setCode("HELLO");
        m2.setData("WORLD     ");

        String e1 = FixedParser.parser().export(m1);
        String e2 = FixedParser.parser().export(m2);

        assertEquals(e1.substring(15, 17), e2.substring(15, 17));
    }

    @Test
    void checksumHandler_differentInputProducesDifferentChecksum() {
        ChecksumModel m1 = new ChecksumModel();
        m1.setCode("AAAAA");
        m1.setData("BBBBBBBBB ");

        ChecksumModel m2 = new ChecksumModel();
        m2.setCode("CCCCC");
        m2.setData("DDDDDDDDD ");

        String e1 = FixedParser.parser().export(m1);
        String e2 = FixedParser.parser().export(m2);

        assertNotEquals(e1.substring(15, 17), e2.substring(15, 17));
    }

    // -------------------------------------------------------------------------
    // Builder accessible from ContextFrame
    // -------------------------------------------------------------------------

    @Test
    void builderAccessibleFromContextFrame() {
        List<Boolean> hasBuilder = new java.util.ArrayList<>();

        FixedParser parser = FixedParser.parser();
        parser.onPhase(Phase.WRITE_AFTER_GET, (ctx) -> {
            hasBuilder.add(ctx.parentFrame().getBuilder() != null);
        });

        ChecksumModel model = new ChecksumModel();
        model.setCode("HELLO");
        model.setData("WORLD     ");
        parser.export(model);

        assertFalse(hasBuilder.isEmpty());
        assertTrue(hasBuilder.stream().allMatch(b -> b));
    }

    // -------------------------------------------------------------------------
    // Builder not available during READ
    // -------------------------------------------------------------------------

    @Test
    void builderIsNullDuringRead() {
        List<Boolean> builderNull = new java.util.ArrayList<>();

        FixedParser parser = FixedParser.parser();
        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            builderNull.add(ctx.parentFrame().getBuilder() == null);
        });

        ChecksumModel model = new ChecksumModel();
        model.setCode("HELLO");
        model.setData("WORLD     ");
        model.setChecksum(42);
        parser.parse(ChecksumModel.class, FixedParser.parser().export(model));

        assertFalse(builderNull.isEmpty());
        assertTrue(builderNull.stream().allMatch(b -> b));
    }
}
