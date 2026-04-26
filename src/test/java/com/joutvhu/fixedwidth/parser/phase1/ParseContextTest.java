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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 — ParseContext
 *
 * Kiểm tra context được tạo, truyền đúng, và frame stack hoạt động chính xác.
 * Tất cả test này sẽ FAIL cho đến khi Phase 1 được implement.
 */
class ParseContextTest {

    // -------------------------------------------------------------------------
    // Context tồn tại trong suốt parse session
    // -------------------------------------------------------------------------

    @Test
    void contextIsCreatedForEachParseCall() {
        // Mỗi lần gọi parse() tạo một context mới độc lập
        List<ParseContext> captured = new ArrayList<>();

        FixedParser parser = FixedParser.parser()
                .onContextCreated(captured::add); // hook để capture context

        parser.parse(SimpleStringModel.class, "hello     ");
        parser.parse(SimpleStringModel.class, "world     ");

        assertEquals(2, captured.size());
        assertNotSame(captured.get(0), captured.get(1));
    }

    @Test
    void contextIsNotSharedBetweenCalls() {
        // Global property từ lần parse trước không leak sang lần sau
        FixedParser parser = FixedParser.parser()
                .onContextCreated(ctx -> ctx.putGlobal("key", "value"));

        ParseContext[] last = new ParseContext[1];
        parser.onContextCreated(ctx -> last[0] = ctx);

        parser.parse(SimpleStringModel.class, "hello     ");
        ParseContext first = last[0];

        parser.parse(SimpleStringModel.class, "world     ");
        ParseContext second = last[0];

        // Hai context khác nhau, property không leak
        assertNotSame(first, second);
    }

    // -------------------------------------------------------------------------
    // Frame stack — thông tin về vị trí trong cây
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

        // Tất cả field đều có parent là MultiFieldModel
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

        // Ít nhất một field có stack depth >= 2
        assertTrue(stacks.stream().anyMatch(s -> s.size() >= 2));
    }

    // -------------------------------------------------------------------------
    // Raw string — chuỗi gốc sau khi cắt
    // -------------------------------------------------------------------------

    @Test
    void rawStringIsExactSubstringBeforeAnyProcessing() {
        // Field "value" start=0, length=10
        // Input: "hello     " → raw = "hello     " (chưa trim)
        FixedParser parser = FixedParser.parser();
        List<String> rawValues = new ArrayList<>();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            rawValues.add(ctx.getRawString());
        });

        parser.parse(SimpleStringModel.class, "hello     ");

        assertEquals(1, rawValues.size());
        assertEquals("hello     ", rawValues.get(0)); // raw giữ nguyên padding
    }

    @Test
    void rawStringIsImmutableThroughoutSession() {
        // getRawString() luôn trả về cùng giá trị dù handler có setProcessedString()
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
        assertEquals("hello     ", rawAtConvert.get(0)); // raw không thay đổi
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
    // Phase — ctx.getPhase() đúng tại mỗi thời điểm
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
    // Custom properties — scoped và global
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
        // Sau khi field frame pop, scoped property không còn ở object frame
        parser.onPhase(Phase.READ_AFTER_OBJECT, (ctx) -> {
            hasKey.add(ctx.has("scopedKey"));
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        assertFalse(hasKey.get(0));
    }

    @Test
    void partialResultContainsAlreadyParsedFields() {
        // Khi parse field thứ 2, partialResult của parent đã có field thứ 1
        FixedParser parser = FixedParser.parser();
        List<Object> partials = new ArrayList<>();
        int[] callCount = {0};

        parser.onPhase(Phase.READ_AFTER_CONVERT, (ctx) -> {
            callCount[0]++;
            if (callCount[0] == 2) { // field thứ 2 (name)
                partials.add(ctx.parentFrame().getPartialResult());
            }
        });

        parser.parse(MultiFieldModel.class, "040hello     Y2024-01-15");

        assertFalse(partials.isEmpty());
        MultiFieldModel partial = (MultiFieldModel) partials.get(0);
        assertEquals(40L, partial.getId()); // field id đã được parse
    }
}
