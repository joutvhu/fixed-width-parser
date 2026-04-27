package com.joutvhu.fixedwidth.parser.phase6;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6 — Debug mode
 */
class DebugModeTest {

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class SimpleModel {
        @FixedField(start = 0, length = 5)
        private String code;

        @FixedField(start = 5, length = 5)
        private Integer amount;
    }

    /**
     * A simple JUL Logger that captures debug messages for assertion.
     * We use a real logger but verify that debug() does not throw.
     */
    @Test
    void debug_doesNotThrow_duringParse() {
        Logger logger = Logger.getLogger(DebugModeTest.class.getName());
        assertDoesNotThrow(() ->
                FixedParser.parser()
                        .debug(logger)
                        .parse(SimpleModel.class, "ABC  00042"));
    }

    @Test
    void debug_doesNotThrow_duringExport() {
        Logger logger = Logger.getLogger(DebugModeTest.class.getName());
        SimpleModel model = new SimpleModel();
        model.setCode("ABC");
        model.setAmount(42);

        assertDoesNotThrow(() ->
                FixedParser.parser()
                        .debug(logger)
                        .export(model));
    }

    @Test
    void debug_parseResult_isCorrect() {
        // Debug mode should not affect parse result
        Logger logger = Logger.getLogger(DebugModeTest.class.getName());
        SimpleModel result = FixedParser.parser()
                .debug(logger)
                .parse(SimpleModel.class, "ABC  00042");

        assertNotNull(result);
        assertEquals("ABC  ", result.getCode()); // keepPadding=AUTO for String → KEEP
        assertEquals(42, result.getAmount());
    }

    @Test
    void debug_exportResult_isCorrect() {
        // Debug mode should not affect export result
        Logger logger = Logger.getLogger(DebugModeTest.class.getName());
        SimpleModel model = new SimpleModel();
        model.setCode("ABC");
        model.setAmount(42);

        String result = FixedParser.parser()
                .debug(logger)
                .export(model);

        assertNotNull(result);
        assertEquals(10, result.length());
        assertTrue(result.startsWith("ABC"));
    }

    @Test
    void debug_canBeChainedWithOtherOptions() {
        Logger logger = Logger.getLogger(DebugModeTest.class.getName());
        // debug() should be chainable with withProperty() etc.
        assertDoesNotThrow(() ->
                FixedParser.parser()
                        .withProperty("test.key", "value")
                        .debug(logger)
                        .parse(SimpleModel.class, "ABC  00042"));
    }

    @Test
    void noDebug_parseResult_isCorrect() {
        // Baseline: without debug, parse works normally
        SimpleModel result = FixedParser.parser()
                .parse(SimpleModel.class, "ABC  00042");

        assertNotNull(result);
        assertEquals(42, result.getAmount());
    }
}
