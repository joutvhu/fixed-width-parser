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
 * Tests for Map field parsing and exporting.
 * Covers: basic Map, SortedMap, empty map, multiple entries,
 * and the mutable-start bug regression (write called twice on same parser).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MapFieldTests {

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
    public static class SimpleMapModel {
        @FixedField(length = 12)
        private Map<@FixedParam(length = 2) String, @FixedParam(length = 4) String> map;
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortedMapModel {
        @FixedField(length = 12)
        private SortedMap<@FixedParam(length = 2) String, @FixedParam(length = 4) String> map;
    }

    // -------------------------------------------------------------------------
    // Parse
    // -------------------------------------------------------------------------

    @Test
    void parseMap_twoEntries() {
        SimpleMapModel m = parser.parse(SimpleMapModel.class, "abCDEFghIJKL");
        assertEquals(2, m.getMap().size());
        assertEquals("CDEF", m.getMap().get("ab"));
        assertEquals("IJKL", m.getMap().get("gh"));
    }

    @Test
    void parseMap_blank_returnsNullObject() {
        // When the entire record is blank, parse() returns null
        assertNull(parser.parse(SimpleMapModel.class, "            "));
    }

    @Test
    void parseSortedMap() {
        SortedMapModel m = parser.parse(SortedMapModel.class, "abCDEFghIJKL");
        assertInstanceOf(SortedMap.class, m.getMap());
        assertEquals("CDEF", m.getMap().get("ab"));
    }

    // -------------------------------------------------------------------------
    // Export
    // -------------------------------------------------------------------------

    @Test
    void exportMap_twoEntries_containsKeyValuePairs() {
        // Use LinkedHashMap for deterministic order
        Map<String, String> map = new LinkedHashMap<>();
        map.put("k1", "val1");
        map.put("k2", "val2");
        String line = parser.export(new SimpleMapModel(map));
        assertTrue(line.contains("k1val1"));
        assertTrue(line.contains("k2val2"));
    }

    @Test
    void exportMap_emptyMap_allSpaces() {
        String line = parser.export(new SimpleMapModel(new HashMap<>()));
        assertEquals("            ", line);
    }

    // -------------------------------------------------------------------------
    // Mutable-start regression: export same model twice must give same result
    // -------------------------------------------------------------------------

    @Test
    void export_calledTwice_sameResult() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("aa", "bbbb");
        SimpleMapModel model = new SimpleMapModel(map);

        String first = parser.export(model);
        String second = parser.export(model);
        assertEquals(first, second);
    }

    // -------------------------------------------------------------------------
    // Round-trip
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_twoEntries() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("k1", "val1");
        map.put("k2", "val2");
        SimpleMapModel original = new SimpleMapModel(map);
        String line = parser.export(original);
        SimpleMapModel parsed = parser.parse(SimpleMapModel.class, line);
        assertEquals(map.get("k1"), parsed.getMap().get("k1"));
        assertEquals(map.get("k2"), parsed.getMap().get("k2"));
    }
}
