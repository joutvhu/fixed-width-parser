package com.joutvhu.fixedwidth.parser.phase5;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.annotation.FixedDelimiter;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.annotation.FixedTerminator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 5 — Collection enhancements
 * <p>
 * Kiểm tra @FixedCount, @FixedDelimiter, @FixedTerminator.
 * Tất cả test này sẽ FAIL cho đến khi Phase 5 được implement.
 */
class CollectionEnhancementTest {

    // -------------------------------------------------------------------------
    // Models
    // -------------------------------------------------------------------------

    /**
     * Fixed count: luôn đúng 3 element
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedCountModel {
        @FixedCount(3)
        @FixedField(length = 15)
        private List<@FixedParam(length = 5) String> items;
    }

    /**
     * Count từ field khác
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountFieldModel {
        @FixedField(start = 0, length = 2)
        private Integer itemCount;

        @FixedCount(field = "itemCount")
        @FixedField(start = 2, length = 0)
        private List<@FixedParam(length = 3) String> items;
    }

    /**
     * Delimiter: tách bằng ","
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DelimiterModel {
        @FixedDelimiter(",")
        @FixedField(length = 20)
        private List<String> items;
    }

    /**
     * Terminator: đọc đến "|"
     */
    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminatorModel {
        @FixedTerminator("|")
        @FixedField(length = 20)
        private List<@FixedParam(length = 3) String> items;
    }

    // -------------------------------------------------------------------------
    // @FixedCount(value) — fixed count
    // -------------------------------------------------------------------------

    @Test
    void fixedCount_parsesExactNumberOfElements() {
        FixedCountModel model = FixedParser.parser()
            .parse(FixedCountModel.class, "AAAAABBBBBCCCCC");

        assertEquals(3, model.getItems().size());
        assertEquals("AAAAA", model.getItems().get(0));
        assertEquals("BBBBB", model.getItems().get(1));
        assertEquals("CCCCC", model.getItems().get(2));
    }

    @Test
    void fixedCount_doesNotReadBeyondCount() {
        // Input có 4 element nhưng count=3 → chỉ đọc 3
        FixedCountModel model = FixedParser.parser()
            .parse(FixedCountModel.class, "AAAAABBBBBCCCCCDDDD");

        assertEquals(3, model.getItems().size());
    }

    @Test
    void fixedCount_exportWritesExactCount() {
        FixedCountModel model = new FixedCountModel(Arrays.asList("AAAAA", "BBBBB", "CCCCC"));
        String exported = FixedParser.parser().export(model);

        assertEquals("AAAAABBBBBCCCCC", exported);
    }

    // -------------------------------------------------------------------------
    // @FixedCount(field) — count từ field khác
    // -------------------------------------------------------------------------

    @Test
    void countField_parsesCorrectNumberOfElements() {
        // "03" = 3 items, rồi 3 items × 3 chars
        CountFieldModel model = FixedParser.parser()
            .parse(CountFieldModel.class, "03AAABBBCCC");

        assertEquals(3, model.getItemCount());
        assertEquals(3, model.getItems().size());
        assertEquals("AAA", model.getItems().get(0));
        assertEquals("BBB", model.getItems().get(1));
        assertEquals("CCC", model.getItems().get(2));
    }

    @Test
    void countField_zeroCount_emptyList() {
        CountFieldModel model = FixedParser.parser()
            .parse(CountFieldModel.class, "00");

        assertEquals(0, model.getItemCount());
        assertTrue(model.getItems().isEmpty());
    }

    @Test
    void countField_exportUpdatesCountField() {
        CountFieldModel model = new CountFieldModel(0, Arrays.asList("AAA", "BBB"));
        // itemCount phải được tự động set thành 2 khi export
        String exported = FixedParser.parser().export(model);

        assertEquals("02", exported.substring(0, 2));
        assertEquals("AAABBB", exported.substring(2));
    }

    // -------------------------------------------------------------------------
    // @FixedDelimiter
    // -------------------------------------------------------------------------

    @Test
    void delimiter_parsesElementsSeparatedByDelimiter() {
        DelimiterModel model = FixedParser.parser()
            .parse(DelimiterModel.class, "AA,BBB,CC,DDDD      ");

        assertEquals(4, model.getItems().size());
        assertEquals("AA", model.getItems().get(0));
        assertEquals("BBB", model.getItems().get(1));
        assertEquals("CC", model.getItems().get(2));
        assertEquals("DDDD", model.getItems().get(3));
    }

    @Test
    void delimiter_exportJoinsWithDelimiter() {
        DelimiterModel model = new DelimiterModel(Arrays.asList("AA", "BBB", "CC"));
        String exported = FixedParser.parser().export(model);

        assertTrue(exported.startsWith("AA,BBB,CC"));
    }

    @Test
    void delimiter_emptyList_exportAllSpaces() {
        DelimiterModel model = new DelimiterModel(List.of());
        String exported = FixedParser.parser().export(model);

        assertEquals("                    ", exported);
    }

    // -------------------------------------------------------------------------
    // @FixedTerminator
    // -------------------------------------------------------------------------

    @Test
    void terminator_stopsAtTerminatorChar() {
        TerminatorModel model = FixedParser.parser()
            .parse(TerminatorModel.class, "AAABBBCCC|          ");

        assertEquals(3, model.getItems().size());
        assertEquals("AAA", model.getItems().get(0));
        assertEquals("BBB", model.getItems().get(1));
        assertEquals("CCC", model.getItems().get(2));
    }

    @Test
    void terminator_noTerminatorFound_readsToEndOfField() {
        TerminatorModel model = FixedParser.parser()
            .parse(TerminatorModel.class, "AAABBBCCCDDDEEEFFFG");

        // Đọc đến hết field length (20 chars / 3 = 6 items, nhưng 19 chars → 6 items + 1 partial)
        assertFalse(model.getItems().isEmpty());
    }

    @Test
    void terminator_exportAppendsTerminator() {
        TerminatorModel model = new TerminatorModel(Arrays.asList("AAA", "BBB"));
        String exported = FixedParser.parser().export(model);

        assertTrue(exported.contains("|"));
        int terminatorIdx = exported.indexOf("|");
        assertEquals("AAABBB", exported.substring(0, terminatorIdx));
    }

    // -------------------------------------------------------------------------
    // Round-trip
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_fixedCount() {
        FixedCountModel original = new FixedCountModel(Arrays.asList("AAAAA", "BBBBB", "CCCCC"));
        String exported = FixedParser.parser().export(original);
        FixedCountModel parsed = FixedParser.parser().parse(FixedCountModel.class, exported);

        assertEquals(original.getItems(), parsed.getItems());
    }

    @Test
    void roundTrip_countField() {
        CountFieldModel original = new CountFieldModel(2, Arrays.asList("AAA", "BBB"));
        String exported = FixedParser.parser().export(original);
        CountFieldModel parsed = FixedParser.parser().parse(CountFieldModel.class, exported);

        assertEquals(original.getItems(), parsed.getItems());
    }
}
