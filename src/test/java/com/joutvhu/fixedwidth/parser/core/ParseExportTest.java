package com.joutvhu.fixedwidth.parser.core;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedPadding;
import com.joutvhu.fixedwidth.parser.annotation.FixedRequired;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.exception.MandatoryValueException;
import com.joutvhu.fixedwidth.parser.model.CollectionModel;
import com.joutvhu.fixedwidth.parser.model.MultiFieldModel;
import com.joutvhu.fixedwidth.parser.model.SubTypeModel;
import com.joutvhu.fixedwidth.parser.support.ItemReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core parse/export tests — basic behavior of the library.
 * These are "smoke tests" to ensure core features work correctly
 * after each phase of refactoring.
 */
class ParseExportTest {

    private final FixedParser parser = FixedParser.parser();

    // -------------------------------------------------------------------------
    // String field
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StringModel {
        @FixedField(length = 10)
        private String value;
    }

    @Test
    void parseString_keepsPaddingByDefault() {
        StringModel m = parser.parse(StringModel.class, "hello     ");
        assertEquals("hello     ", m.getValue()); // keepPadding=AUTO → KEEP cho String
    }

    @Test
    void parseString_blank_returnsNull() {
        assertNull(parser.parse(StringModel.class, "          "));
    }

    @Test
    void exportString_rightPaddedToLength() {
        assertEquals("hello     ", parser.export(new StringModel("hello")));
    }

    @Test
    void exportString_null_allSpaces() {
        assertEquals("          ", parser.export(new StringModel(null)));
    }

    @Test
    void exportString_tooLong_truncated() {
        assertEquals("helloworld", parser.export(new StringModel("helloworldXXX")));
    }

    // -------------------------------------------------------------------------
    // Number field
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NumberModel {
        @FixedField(length = 5)
        private Long longVal;

        @FixedField(start = 5, length = 10)
        private Double doubleVal;
    }

    @Test
    void parseNumber_zeroPadded() {
        NumberModel m = parser.parse(NumberModel.class, "00042000001.618");
        assertEquals(42L, m.getLongVal());
        assertEquals(1.618, m.getDoubleVal(), 0.001);
    }

    @Test
    void exportNumber_zeroPaddedRightAligned() {
        NumberModel m = new NumberModel(42L, 1.618);
        String exported = parser.export(m);
        assertEquals("00042", exported.substring(0, 5));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 99999L})
    void roundTrip_long(long value) {
        NumberModel original = new NumberModel(value, 0.0);
        String exported = parser.export(original);
        NumberModel parsed = parser.parse(NumberModel.class, exported);
        assertEquals(value, parsed.getLongVal());
    }

    // -------------------------------------------------------------------------
    // Boolean field
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoolModel {
        @FixedFormat(format = "Y|N")
        @FixedField(length = 1)
        private Boolean flag;
    }

    @Test
    void parseBoolean_Y_returnsTrue() {
        assertEquals(true, parser.parse(BoolModel.class, "Y").getFlag());
    }

    @Test
    void parseBoolean_N_returnsFalse() {
        assertEquals(false, parser.parse(BoolModel.class, "N").getFlag());
    }

    @Test
    void exportBoolean_true_returnsY() {
        assertEquals("Y", parser.export(new BoolModel(true)));
    }

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomBoolModel {
        @com.joutvhu.fixedwidth.parser.constraint.FixedBoolean(trueValues = {"Có", "YES"}, falseValues = {"Không", "NO"})
        @FixedField(length = 5)
        private Boolean flag;
    }

    @Test
    void parseCustomBoolean_trueValue_returnsTrue() {
        assertEquals(true, parser.parse(CustomBoolModel.class, "Có   ").getFlag());
    }

    @Test
    void parseCustomBoolean_falseValue_returnsFalse() {
        assertEquals(false, parser.parse(CustomBoolModel.class, "Không").getFlag());
    }

    @Test
    void exportCustomBoolean_true_returnsFirstTrueValue() {
        assertEquals("Có   ", parser.export(new CustomBoolModel(true)));
    }

    @Test
    void exportCustomBoolean_false_returnsFirstFalseValue() {
        assertEquals("Không", parser.export(new CustomBoolModel(false)));
    }

    // -------------------------------------------------------------------------
    // Date field
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateModel {
        @FixedFormat(format = "yyyy-MM-dd HH:mm:ss")
        @FixedField(length = 19)
        private LocalDateTime dateTime;
    }

    @Test
    void parseDateTime() {
        DateModel m = parser.parse(DateModel.class, "2024-01-15 14:30:00");
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 30, 0), m.getDateTime());
    }

    @Test
    void exportDateTime() {
        DateModel m = new DateModel(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
        assertEquals("2024-01-15 14:30:00", parser.export(m));
    }

    // -------------------------------------------------------------------------
    // Alignment
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlignmentModel {
        @FixedPadding(alignment = Alignment.LEFT)
        @FixedField(length = 10)
        private String left;

        @FixedPadding(alignment = Alignment.RIGHT)
        @FixedField(start = 10, length = 10)
        private String right;

        @FixedPadding(alignment = Alignment.CENTER)
        @FixedField(start = 20, length = 10)
        private String center;
    }

    @Test
    void exportAlignment_left() {
        AlignmentModel m = new AlignmentModel("Bob", null, null);
        assertEquals("Bob       ", parser.export(m).substring(0, 10));
    }

    @Test
    void exportAlignment_right() {
        AlignmentModel m = new AlignmentModel(null, "Bob", null);
        assertEquals("       Bob", parser.export(m).substring(10, 20));
    }

    @Test
    void exportAlignment_center_evenPadSize() {
        // "ab" len=2, size=10, padSize=8 → left=4, right=4
        AlignmentModel m = new AlignmentModel(null, null, "ab");
        assertEquals("    ab    ", parser.export(m).substring(20, 30));
    }

    @Test
    void exportAlignment_center_oddPadSize() {
        // "abc" len=3, size=10, padSize=7 → left=3, right=4
        AlignmentModel m = new AlignmentModel(null, null, "abc");
        assertEquals("   abc    ", parser.export(m).substring(20, 30));
    }

    // -------------------------------------------------------------------------
    // KeepPadding
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeepPaddingModel {
        @FixedPadding(keep = KeepPadding.DROP)
        @FixedField(length = 10)
        private String dropped;

        @FixedPadding(keep = KeepPadding.KEEP)
        @FixedField(start = 10, length = 10)
        private String kept;
    }

    @Test
    void parseKeepPadding_drop_trimsPadding() {
        KeepPaddingModel m = parser.parse(KeepPaddingModel.class, "hello     hello     ");
        assertEquals("hello", m.getDropped());
    }

    @Test
    void parseKeepPadding_keep_retainsPadding() {
        KeepPaddingModel m = parser.parse(KeepPaddingModel.class, "hello     hello     ");
        assertEquals("hello     ", m.getKept());
    }

    // -------------------------------------------------------------------------
    // Multi-field model
    // -------------------------------------------------------------------------

    @Test
    void parseMultiField() {
        MultiFieldModel m = parser.parse(MultiFieldModel.class, "040hello     Y2024-01-15");
        assertEquals(40L, m.getId());
        assertEquals("hello     ", m.getName());
        assertEquals(true, m.getActive());
        assertEquals(LocalDate.of(2024, 1, 15), m.getDate());
    }

    @Test
    void exportMultiField() {
        MultiFieldModel m = new MultiFieldModel(40L, "hello     ", true, LocalDate.of(2024, 1, 15));
        assertEquals("040hello     Y2024-01-15", parser.export(m));
    }

    // -------------------------------------------------------------------------
    // Subtype detection
    // -------------------------------------------------------------------------

    @Test
    void parseSubType_oneOf_selectsTypeA() {
        SubTypeModel result = parser.parse(SubTypeModel.class, "Ahello");
        assertInstanceOf(SubTypeModel.TypeA.class, result);
        assertEquals("hell", ((SubTypeModel.TypeA) result).getDataA());
    }

    @Test
    void parseSubType_matchWith_selectsTypeB() {
        SubTypeModel result = parser.parse(SubTypeModel.class, "Bworld");
        assertInstanceOf(SubTypeModel.TypeB.class, result);
    }

    @Test
    void parseSubType_noMatch_usesDefault() {
        SubTypeModel result = parser.parse(SubTypeModel.class, "Zfallback");
        assertInstanceOf(SubTypeModel.TypeC.class, result);
    }

    // -------------------------------------------------------------------------
    // Collection field
    // -------------------------------------------------------------------------

    @Test
    void parseCollection() {
        CollectionModel m = parser.parse(CollectionModel.class, "abcdefghiabCDEFghIJKL");
        assertEquals(3, m.getItems().size());
        assertEquals("abc", m.getItems().get(0));
    }

    @Test
    void exportCollection() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("k1", "val1");
        CollectionModel m = new CollectionModel(Arrays.asList("abc", "def", "ghi"), map);
        String exported = parser.export(m);
        assertTrue(exported.startsWith("abcdefghi"));
    }

    // -------------------------------------------------------------------------
    // InputStream parsing
    // -------------------------------------------------------------------------

    @Test
    void parseInputStream_readsAllLines() throws IOException {
        String content = "040hello     Y2024-01-15\n040world     N2024-06-01";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        ItemReader<MultiFieldModel> reader = parser.parse(MultiFieldModel.class, is);

        assertTrue(reader.hasNext());
        MultiFieldModel first = reader.next();
        assertEquals(40L, first.getId());

        assertTrue(reader.hasNext());
        MultiFieldModel second = reader.next();
        assertEquals(40L, second.getId());

        assertFalse(reader.hasNext());
        reader.close();
    }

    @Test
    void itemReader_hasNext_idempotent() throws IOException {
        String content = "040hello     Y2024-01-15";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        ItemReader<MultiFieldModel> reader = parser.parse(MultiFieldModel.class, is);

        assertTrue(reader.hasNext());
        assertTrue(reader.hasNext()); // multiple calls do not advance the cursor
        assertTrue(reader.hasNext());
        assertNotNull(reader.next());
        assertFalse(reader.hasNext());
        reader.close();
    }

    // -------------------------------------------------------------------------
    // Stream parsing/export
    // -------------------------------------------------------------------------

    @Test
    void parseStream_returnsAllItems() {
        Stream<String> input = Stream.of(
            "040hello     Y2024-01-15",
            "099world     N2024-06-01");

        List<MultiFieldModel> results = parser.parse(MultiFieldModel.class, input)
            .collect(Collectors.toList());

        assertEquals(2, results.size());
        assertEquals(40L, results.get(0).getId());
        assertEquals(99L, results.get(1).getId());
    }

    @Test
    void exportStream_returnsAllLines() {
        Stream<MultiFieldModel> input = Stream.of(
            new MultiFieldModel(40L, "hello     ", true, LocalDate.of(2024, 1, 15)),
            new MultiFieldModel(99L, "world     ", false, LocalDate.of(2024, 6, 1)));

        List<String> lines = parser.export(input).collect(Collectors.toList());

        assertEquals(2, lines.size());
        assertEquals("040hello     Y2024-01-15", lines.get(0));
    }

    // -------------------------------------------------------------------------
    // Null argument guards
    // -------------------------------------------------------------------------

    @Test
    void parse_nullType_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.parse((Class<StringModel>) null, "hello     "));
    }

    @Test
    void parse_nullLine_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.parse(StringModel.class, (String) null));
    }

    @Test
    void export_nullObject_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> parser.export((StringModel) null));
    }

    // -------------------------------------------------------------------------
    // Required field
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredModel {
        @FixedRequired
        @FixedField(length = 5)
        private String value;
    }

    @Test
    void exportRequired_null_throws() {
        assertThrows(MandatoryValueException.class,
            () -> parser.export(new RequiredModel(null)));
    }

    // -------------------------------------------------------------------------
    // DecimalFormat
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormattedNumberModel {
        @FixedFormat(format = "#,###,###")
        @FixedField(length = 9)
        private Integer value;
    }

    @Test
    void parseFormattedNumber() {
        FormattedNumberModel m = parser.parse(FormattedNumberModel.class, "1,741,111");
        assertEquals(1741111, m.getValue());
    }

    @Test
    void exportFormattedNumber() {
        assertEquals("1,741,111", parser.export(new FormattedNumberModel(1741111)));
    }

    // -------------------------------------------------------------------------
    // BigDecimal
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BigDecimalModel {
        @FixedField(length = 15)
        private BigDecimal value;
    }

    @Test
    void roundTrip_bigDecimal() {
        BigDecimalModel original = new BigDecimalModel(new BigDecimal("123456789.12345"));
        String exported = parser.export(original);
        BigDecimalModel parsed = parser.parse(BigDecimalModel.class, exported);
        assertEquals(original.getValue(), parsed.getValue());
    }
}
