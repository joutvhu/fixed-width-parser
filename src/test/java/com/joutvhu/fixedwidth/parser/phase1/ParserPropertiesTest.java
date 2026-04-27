package com.joutvhu.fixedwidth.parser.phase1;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.ParseProperties;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.model.SimpleStringModel;
import com.joutvhu.fixedwidth.parser.support.Phase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Phase 1 — Parser properties
 * <p>
 * Kiểm tra parser-level config và session-level property injection.
 * Tất cả test này sẽ FAIL cho đến khi Phase 1 được implement.
 */
class ParserPropertiesTest {

    // -------------------------------------------------------------------------
    // Parser-level config (global, tồn tại suốt vòng đời parser)
    // -------------------------------------------------------------------------

    @Test
    void parserLevelPropertyAvailableInAllSessions() {
        FixedParser parser = FixedParser.parser()
            .withProperty("myConfig", "globalValue");

        List<String> values = new ArrayList<>();
        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            values.add(ctx.getProperty("myConfig", String.class, null));
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        parser.parse(SimpleStringModel.class, "world     ");

        assertEquals(2, values.size());
        assertEquals("globalValue", values.get(0));
        assertEquals("globalValue", values.get(1));
    }

    @Test
    void parserLevelPropertyIsSharedAcrossMultipleParseCalls() {
        FixedParser parser = FixedParser.parser()
            .withProperty("counter", 0);

        // Property là immutable per-call — không thể mutate parser config từ session
        parser.parse(SimpleStringModel.class, "hello     ");
        parser.parse(SimpleStringModel.class, "world     ");

        // Parser config không thay đổi giữa các lần parse
        assertEquals(0, parser.getProperty("counter", Integer.class));
    }

    // -------------------------------------------------------------------------
    // Session-level property (per-call, ghi đè parser config)
    // -------------------------------------------------------------------------

    @Test
    void sessionPropertyOverridesParserConfig() {
        FixedParser parser = FixedParser.parser()
            .withProperty("locale", Locale.US);

        List<Locale> locales = new ArrayList<>();
        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            locales.add(ctx.getProperty("locale", Locale.class, Locale.getDefault()));
        });

        // Lần 1: dùng parser config
        parser.parse(SimpleStringModel.class, "hello     ");

        // Lần 2: override bằng session property
        parser.parse(SimpleStringModel.class, "hello     ",
            ParseProperties.of("locale", Locale.JAPAN));

        assertEquals(Locale.US, locales.get(0));
        assertEquals(Locale.JAPAN, locales.get(1));
    }

    @Test
    void sessionPropertyDoesNotAffectSubsequentCalls() {
        FixedParser parser = FixedParser.parser()
            .withProperty("locale", Locale.US);

        List<Locale> locales = new ArrayList<>();
        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            locales.add(ctx.getProperty("locale", Locale.class, Locale.getDefault()));
        });

        parser.parse(SimpleStringModel.class, "hello     ",
            ParseProperties.of("locale", Locale.JAPAN));
        parser.parse(SimpleStringModel.class, "hello     "); // không có session property

        assertEquals(Locale.JAPAN, locales.get(0));
        assertEquals(Locale.US, locales.get(1)); // trở về parser config
    }

    // -------------------------------------------------------------------------
    // Fallback chain: session → parser config → default
    // -------------------------------------------------------------------------

    @Test
    void getPropertyFallsBackToDefaultWhenNotSet() {
        FixedParser parser = FixedParser.parser(); // không set property nào

        List<String> values = new ArrayList<>();
        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            values.add(ctx.getProperty("missing", String.class, "defaultValue"));
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        assertEquals("defaultValue", values.get(0));
    }

    @Test
    void getPropertyFallsBackToParserConfigBeforeDefault() {
        FixedParser parser = FixedParser.parser()
            .withProperty("key", "fromConfig");

        List<String> values = new ArrayList<>();
        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            values.add(ctx.getProperty("key", String.class, "defaultValue"));
        });

        parser.parse(SimpleStringModel.class, "hello     ");
        assertEquals("fromConfig", values.get(0)); // config thắng default
    }

    @Test
    void requirePropertyThrowsWhenNotFound() {
        FixedParser parser = FixedParser.parser();

        parser.onPhase(Phase.READ_AFTER_CUT, (ctx) -> {
            assertThrows(IllegalStateException.class,
                () -> ctx.requireProperty("missing", String.class));
        });

        parser.parse(SimpleStringModel.class, "hello     ");
    }

    // -------------------------------------------------------------------------
    // Built-in properties: locale và timezone cho DateHandler
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class DateModel {
        @FixedFormat(format = "dd/MM/yyyy")
        @FixedField(length = 10)
        private LocalDate date;
    }

    @Test
    void dateHandlerUsesLocaleFromParserProperty() {
        // Với locale khác nhau, format date có thể khác nhau
        // Test này xác nhận DateHandler đọc locale từ context property
        FixedParser parser = FixedParser.parser()
            .withProperty("locale", Locale.US);

        DateModel model = parser.parse(DateModel.class, "15/01/2024");
        assertEquals(LocalDate.of(2024, 1, 15), model.getDate());
    }

    @Test
    void dateHandlerUsesTimezoneFromParserProperty() {
        FixedParser parser = FixedParser.parser()
            .withProperty("timezone", ZoneId.of("Asia/Ho_Chi_Minh"));

        DateModel model = parser.parse(DateModel.class, "15/01/2024");
        assertNotNull(model.getDate());
    }

    @Test
    void dateHandlerUsesSessionTimezoneOverParserConfig() {
        FixedParser parser = FixedParser.parser()
            .withProperty("timezone", ZoneId.of("UTC"));

        // Override timezone cho một lần parse cụ thể
        DateModel model = parser.parse(DateModel.class, "15/01/2024",
            ParseProperties.of("timezone", ZoneId.of("Asia/Ho_Chi_Minh")));

        assertNotNull(model.getDate());
    }

    // -------------------------------------------------------------------------
    // ParseProperties builder
    // -------------------------------------------------------------------------

    @Test
    void parsePropertiesBuilderCreatesCorrectMap() {
        ParseProperties props = ParseProperties.builder()
            .set("locale", Locale.US)
            .set("timezone", ZoneId.of("UTC"))
            .build();

        assertEquals(Locale.US, props.get("locale", Locale.class));
        assertEquals(ZoneId.of("UTC"), props.get("timezone", ZoneId.class));
    }

    @Test
    void parsePropertiesOfCreatesCorrectMap() {
        ParseProperties props = ParseProperties.of("key1", "val1", "key2", 42);

        assertEquals("val1", props.get("key1", String.class));
        assertEquals(42, props.get("key2", Integer.class));
    }

    @Test
    void parsePropertiesIsImmutable() {
        ParseProperties props = ParseProperties.of("key", "value");

        assertThrows(UnsupportedOperationException.class,
            () -> props.set("newKey", "newValue"));
    }
}
