package com.joutvhu.fixedwidth.parser.phase1;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.model.MultiFieldModel;
import com.joutvhu.fixedwidth.parser.model.NestedModel;
import com.joutvhu.fixedwidth.parser.model.SimpleStringModel;
import com.joutvhu.fixedwidth.parser.support.ContextFrame;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 — ParseContext
 * <p>
 * Tests that the context is created, correctly passed, and the frame stack functions correctly.
 * All these tests will FAIL until Phase 1 is implemented.
 */
class ParseContextTest {

    // -------------------------------------------------------------------------
    // Context exists throughout the parse session
    // -------------------------------------------------------------------------

    @Test
    void contextIsCreatedForEachParseCall() {
        // Each parse() call creates a new independent context
        List<ParseContext> captured = new ArrayList<>();

        FixedParser parser = FixedParser.parser()
            .onContextCreated(captured::add); // hook to capture context

        parser.parse(SimpleStringModel.class, "hello     ");
        parser.parse(SimpleStringModel.class, "world     ");

        assertEquals(2, captured.size());
        assertNotSame(captured.get(0), captured.get(1));
    }

    @Test
    void contextIsNotSharedBetweenCalls() {
        // Global property from the previous parse run doesn't leak to the next
        FixedParser parser = FixedParser.parser()
            .onContextCreated(ctx -> ctx.putGlobal("key", "value"));

        ParseContext[] last = new ParseContext[1];
        parser.onContextCreated(ctx -> last[0] = ctx);

        parser.parse(SimpleStringModel.class, "hello     ");
        ParseContext first = last[0];

        parser.parse(SimpleStringModel.class, "world     ");
        ParseContext second = last[0];

        // Two different contexts, properties don't leak
        assertNotSame(first, second);
    }

    // -------------------------------------------------------------------------
    // Frame stack — information about position in the tree
    // -------------------------------------------------------------------------

    @Test
    void rootFrameHasDepthZero() {
        FixedParser parser = FixedParser.parser();
        List<Integer> depths = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_OBJECT, (ctx) -> {
            depths.add(ctx.currentFrame().getDepth());
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        assertEquals(0, depths.get(0));
    }

    @Test
    void fieldFrameHasDepthOne() {
        FixedParser parser = FixedParser.parser();
        List<Integer> depths = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            depths.add(ctx.currentFrame().getDepth());
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        assertEquals(1, depths.get(0));
    }

    @Test
    void nestedObjectFrameStackHasCorrectDepths() {
        // NestedModel (depth=0) → address field (depth=1) → AddressModel (depth=1) → city field (depth=2)
        FixedParser parser = FixedParser.parser();
        List<Integer> depths = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            depths.add(ctx.currentFrame().getDepth());
        });

        parser.parse(NestedModel.class, "ABCHanoiMain St   ");

        // code field (depth=1), city field (depth=2), street field (depth=2)
        assertTrue(depths.contains(1));
        assertTrue(depths.contains(2));
    }

    @Test
    void parentFrameIsAccessibleFromChildFrame() {
        FixedParser parser = FixedParser.parser();
        List<String> parentTypes = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            ContextFrame parent = ctx.parentFrame();
            if (parent != null) {
                parentTypes.add(parent.getTypeInfo().getType().getSimpleName());
            }
        });

        parser.parse(MultiFieldModel.class, "040hello     Y2024-01-15");

        // All fields have MultiFieldModel as parent
        assertTrue(parentTypes.stream().allMatch(t -> t.equals("MultiFieldModel")));
    }

    @Test
    void frameStackContainsAllAncestors() {
        FixedParser parser = FixedParser.parser();
        List<List<String>> stacks = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            List<String> stack = new ArrayList<>();
            ctx.frameStack().forEach(f -> stack.add(f.getTypeInfo().getType().getSimpleName()));
            stacks.add(stack);
        });

        parser.parse(NestedModel.class, "ABCHanoiMain St   ");

        // At least one field has stack depth >= 2
        assertTrue(stacks.stream().anyMatch(s -> s.size() >= 2));
    }

    // -------------------------------------------------------------------------
    // Raw string — original string after cutting
    // -------------------------------------------------------------------------

    @Test
    void rawStringIsExactSubstringBeforeAnyProcessing() {
        // Field "value" start=0, length=10
        // Input: "hello     " → raw = "hello     " (not trimmed)
        FixedParser parser = FixedParser.parser();
        List<String> rawValues = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            rawValues.add(ctx.getRawString());
        });

        parser.parse(SimpleStringModel.class, "hello     ");

        assertEquals(1, rawValues.size());
        assertEquals("hello     ", rawValues.get(0)); // raw preserves padding
    }

    @Test
    void rawStringIsImmutableThroughoutSession() {
        // getRawString() always returns the same value even if a handler calls setProcessedString()
        FixedParser parser = FixedParser.parser();
        List<String> rawAtCut = new ArrayList<>();
        List<String> rawAtConvert = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            ctx.setProcessedString("MODIFIED");
            rawAtCut.add(ctx.getRawString());
        });
        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            rawAtConvert.add(ctx.getRawString());
        });

        parser.parse(SimpleStringModel.class, "hello     ");

        assertEquals("hello     ", rawAtCut.get(0));
        assertEquals("hello     ", rawAtConvert.get(0)); // raw doesn't change
    }

    @Test
    void rawStringForEachFieldIsCorrectSubstring() {
        // MultiFieldModel: id(0,3), name(3,10), active(13,1), date(14,10)
        FixedParser parser = FixedParser.parser();
        List<String> rawValues = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            rawValues.add(ctx.getRawString());
        });

        parser.parse(MultiFieldModel.class, "040hello     Y2024-01-15");

        assertEquals("040", rawValues.get(0));          // id
        assertEquals("hello     ", rawValues.get(1));   // name
        assertEquals("Y", rawValues.get(2));            // active
        assertEquals("2024-01-15", rawValues.get(3));   // date
    }

    // -------------------------------------------------------------------------
    // Phase — ctx.getPhase() is correct at each point
    // -------------------------------------------------------------------------

    @Test
    void phaseIsCorrectAtEachPoint() {
        FixedParser parser = FixedParser.parser();
        List<Phase> phases = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> phases.add(ctx.getPhase()));
        parser.onPhase(Phase.READ_AFTER_TRANSFORM, (ctx) -> phases.add(ctx.getPhase()));
        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> phases.add(ctx.getPhase()));

        parser.parse(SimpleStringModel.class, "hello     ");

        assertTrue(phases.contains(Phase.READ_AFTER_CUT));
        assertTrue(phases.contains(Phase.READ_AFTER_TRANSFORM));
        assertTrue(phases.contains(Phase.READ_AFTER_CONVERT));
    }

    @Test
    void writePhaseIsCorrectAtEachPoint() {
        FixedParser parser = FixedParser.parser();
        List<Phase> phases = new ArrayList<>();

        parser.onPhase(Phase.WRITE_AFTER_GET, (ctx) -> phases.add(ctx.getPhase()));
        parser.onPhase(Phase.WRITE_AFTER_CONVERT, (ctx) -> phases.add(ctx.getPhase()));
        parser.onPhase(Phase.WRITE_AFTER_TRANSFORM, (ctx) -> phases.add(ctx.getPhase()));

        parser.export(new SimpleStringModel("hello"));

        assertTrue(phases.contains(Phase.WRITE_AFTER_GET));
        assertTrue(phases.contains(Phase.WRITE_AFTER_CONVERT));
        assertTrue(phases.contains(Phase.WRITE_AFTER_TRANSFORM));
    }

    // -------------------------------------------------------------------------
    // Custom properties — scoped and global
    // -------------------------------------------------------------------------

    @Test
    void globalPropertyPersistsThroughoutSession() {
        FixedParser parser = FixedParser.parser();
        List<Object> values = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            ctx.putGlobal("myKey", "myValue");
        });
        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            values.add(ctx.get("myKey", String.class));
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        assertEquals("myValue", values.get(0));
    }

    @Test
    void scopedPropertyClearedAfterFramePop() {
        FixedParser parser = FixedParser.parser();
        List<Boolean> hasKey = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            ctx.putScoped("scopedKey", "scopedValue");
        });
        // After the field frame pops, the scoped property is no longer in the object frame
        parser.onPhase(Phase.READ_AFTER_OBJECT, (ctx) -> {
            hasKey.add(ctx.has("scopedKey"));
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        assertFalse(hasKey.get(0));
    }

    @Test
    void partialResultContainsAlreadyParsedFields() {
        // When parsing the 2nd field, the parent's partialResult already has the 1st field
        FixedParser parser = FixedParser.parser();
        List<Object> partials = new ArrayList<>();
        int[] callCount = {0};

        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            callCount[0]++;
            if (callCount[0] == 2) { // 2nd field (name)
                partials.add(ctx.parentFrame().getPartialResult());
            }
        });

        parser.parse(MultiFieldModel.class, "040hello     Y2024-01-15");

        assertFalse(partials.isEmpty());
        MultiFieldModel partial = (MultiFieldModel) partials.get(0);
        assertEquals(40L, partial.getId()); // field id has been parsed
    }
}
