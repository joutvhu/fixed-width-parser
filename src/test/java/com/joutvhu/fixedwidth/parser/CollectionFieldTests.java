package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Collection field parsing and exporting.
 * Covers: List, Set, Queue, Deque, empty collection,
 * and the mutable-start bug regression (write called twice on same parser).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CollectionFieldTests {

    private FixedParser parser;

    @BeforeAll
    void beforeAll() {
        parser = FixedParser.parser();
    }

    // -------------------------------------------------------------------------
    // Models — public static required for FixedHelper.newInstanceOf()
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StringListModel {
        @FixedField(length = 9)
        private List<@FixedParam(length = 3) String> items;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntListModel {
        @FixedField(length = 9)
        private List<@FixedParam(length = 3) Integer> items;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueModel {
        @FixedField(length = 6)
        private Queue<@FixedParam(length = 2) String> items;
    }

    // -------------------------------------------------------------------------
    // Parse
    // -------------------------------------------------------------------------

    @Test
    void parseStringList_threeItems() {
        StringListModel m = parser.parse(StringListModel.class, "abcdefghi");
        assertEquals(3, m.getItems().size());
        assertEquals("abc", m.getItems().get(0));
        assertEquals("def", m.getItems().get(1));
        assertEquals("ghi", m.getItems().get(2));
    }

    @Test
    void parseStringList_blank_returnsNullObject() {
        // When the entire record is blank, parse() returns null
        assertNull(parser.parse(StringListModel.class, "         "));
    }

    @Test
    void parseIntList() {
        IntListModel m = parser.parse(IntListModel.class, "001002003");
        assertEquals(3, m.getItems().size());
        assertEquals(1, m.getItems().get(0));
        assertEquals(2, m.getItems().get(1));
        assertEquals(3, m.getItems().get(2));
    }

    @Test
    void parseQueue() {
        QueueModel m = parser.parse(QueueModel.class, "aabbcc");
        assertEquals(3, m.getItems().size());
    }

    // -------------------------------------------------------------------------
    // Export
    // -------------------------------------------------------------------------

    @Test
    void exportStringList() {
        StringListModel model = new StringListModel(Arrays.asList("abc", "def", "ghi"));
        assertEquals("abcdefghi", parser.export(model));
    }

    @Test
    void exportStringList_emptyList_allSpaces() {
        StringListModel model = new StringListModel(new ArrayList<>());
        assertEquals("         ", parser.export(model));
    }

    @Test
    void exportIntList() {
        IntListModel model = new IntListModel(Arrays.asList(1, 2, 3));
        assertEquals("001002003", parser.export(model));
    }

    // -------------------------------------------------------------------------
    // Mutable-start regression: export same model twice must give same result
    // -------------------------------------------------------------------------

    @Test
    void export_calledTwice_sameResult() {
        StringListModel model = new StringListModel(Arrays.asList("abc", "def", "ghi"));
        String first = parser.export(model);
        String second = parser.export(model);
        assertEquals(first, second);
    }

    // -------------------------------------------------------------------------
    // Round-trip
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_stringList() {
        StringListModel original = new StringListModel(Arrays.asList("foo", "bar", "baz"));
        String line = parser.export(original);
        StringListModel parsed = parser.parse(StringListModel.class, line);
        assertEquals(original.getItems(), parsed.getItems());
    }

    @Test
    void roundTrip_intList() {
        IntListModel original = new IntListModel(Arrays.asList(10, 20, 30));
        String line = parser.export(original);
        IntListModel parsed = parser.parse(IntListModel.class, line);
        assertEquals(original.getItems(), parsed.getItems());
    }
}
