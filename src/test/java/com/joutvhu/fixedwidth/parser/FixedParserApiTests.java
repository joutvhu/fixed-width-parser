package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.support.ItemReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FixedParser public API surface.
 * Covers: parse(Class, String), parse(Class, Stream), parse(Class, InputStream),
 * parse(Class, InputStream, encoding), export(T), export(Stream),
 * export(Class, Stream), null argument guards, and ItemReader idempotency.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FixedParserApiTests {

    // -------------------------------------------------------------------------
    // Model — public static required for FixedHelper.newInstanceOf()
    // -------------------------------------------------------------------------

    @FixedObject
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleModel {
        @FixedField(length = 3)
        private String code;
        @FixedField(start = 3, length = 4)
        private Integer value;
    }

    private static final String LINE_1 = "ABC0042";
    private static final String LINE_2 = "XYZ0099";

    // -------------------------------------------------------------------------
    // parse(Class, String)
    // -------------------------------------------------------------------------

    @Test
    void parse_classAndString() {
        SimpleModel m = FixedParser.parser().parse(SimpleModel.class, LINE_1);
        assertEquals("ABC", m.getCode());
        assertEquals(42, m.getValue());
    }

    @Test
    void parse_nullType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FixedParser.parser().parse((Class<SimpleModel>) null, LINE_1));
    }

    @Test
    void parse_nullLine_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FixedParser.parser().parse(SimpleModel.class, (String) null));
    }

    // -------------------------------------------------------------------------
    // parse(Class, Stream<String>)
    // -------------------------------------------------------------------------

    @Test
    void parse_stream_returnsAllItems() {
        Stream<String> input = Stream.of(LINE_1, LINE_2);
        List<SimpleModel> results = FixedParser.parser()
                .parse(SimpleModel.class, input)
                .collect(Collectors.toList());
        assertEquals(2, results.size());
        assertEquals("ABC", results.get(0).getCode());
        assertEquals("XYZ", results.get(1).getCode());
    }

    @Test
    void parse_nullStream_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FixedParser.parser().parse(SimpleModel.class, (Stream<String>) null));
    }

    // -------------------------------------------------------------------------
    // parse(Class, InputStream) and parse(Class, InputStream, encoding)
    // -------------------------------------------------------------------------

    @Test
    void parse_inputStream_readsAllLines() throws IOException {
        String content = LINE_1 + "\n" + LINE_2;
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        ItemReader<SimpleModel> reader = FixedParser.parser().parse(SimpleModel.class, is);

        assertTrue(reader.hasNext());
        assertEquals("ABC", reader.next().getCode());
        assertTrue(reader.hasNext());
        assertEquals("XYZ", reader.next().getCode());
        assertFalse(reader.hasNext());
        reader.close();
    }

    @Test
    void parse_inputStreamWithEncoding_readsAllLines() throws IOException {
        String content = LINE_1 + "\n" + LINE_2;
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        ItemReader<SimpleModel> reader = FixedParser.parser().parse(SimpleModel.class, is, "UTF-8");

        assertTrue(reader.hasNext());
        reader.next();
        assertTrue(reader.hasNext());
        reader.next();
        assertFalse(reader.hasNext());
        reader.close();
    }

    @Test
    void parse_nullInputStream_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FixedParser.parser().parse(SimpleModel.class, (InputStream) null));
    }

    // -------------------------------------------------------------------------
    // export(T)
    // -------------------------------------------------------------------------

    @Test
    void export_object() {
        assertEquals(LINE_1, FixedParser.parser().export(new SimpleModel("ABC", 42)));
    }

    @Test
    void export_nullObject_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FixedParser.parser().export((SimpleModel) null));
    }

    // -------------------------------------------------------------------------
    // export(Stream<T>)
    // -------------------------------------------------------------------------

    @Test
    void export_stream_returnsAllLines() {
        Stream<SimpleModel> input = Stream.of(
                new SimpleModel("ABC", 42),
                new SimpleModel("XYZ", 99));
        List<String> lines = FixedParser.parser()
                .export(input)
                .collect(Collectors.toList());
        assertEquals(2, lines.size());
        assertEquals(LINE_1, lines.get(0));
        assertEquals(LINE_2, lines.get(1));
    }

    @Test
    void export_streamWithNullElement_returnsNull() {
        Stream<SimpleModel> input = Arrays.asList(new SimpleModel("ABC", 42), null).stream();
        List<String> lines = FixedParser.parser()
                .export(input)
                .collect(Collectors.toList());
        assertEquals(2, lines.size());
        assertNull(lines.get(1));
    }

    @Test
    void export_nullStream_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FixedParser.parser().export((Stream<SimpleModel>) null));
    }

    // -------------------------------------------------------------------------
    // export(Class, Stream<T>)
    // -------------------------------------------------------------------------

    @Test
    void export_classAndStream() {
        Stream<SimpleModel> input = Stream.of(new SimpleModel("ABC", 42));
        List<String> lines = FixedParser.parser()
                .export(SimpleModel.class, input)
                .collect(Collectors.toList());
        assertEquals(LINE_1, lines.get(0));
    }

    @Test
    void export_classAndNullStream_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FixedParser.parser().export(SimpleModel.class, (Stream<SimpleModel>) null));
    }

    // -------------------------------------------------------------------------
    // Deprecated parse(String, Class) still works
    // -------------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    @Test
    void parse_deprecatedSignature_stillWorks() {
        SimpleModel m = FixedParser.parser().parse(LINE_1, SimpleModel.class);
        assertEquals("ABC", m.getCode());
    }

    // -------------------------------------------------------------------------
    // ItemReader.hasNext() idempotent
    // -------------------------------------------------------------------------

    @Test
    void itemReader_hasNext_idempotent() throws IOException {
        InputStream is = new ByteArrayInputStream(LINE_1.getBytes(StandardCharsets.UTF_8));
        ItemReader<SimpleModel> reader = FixedParser.parser().parse(SimpleModel.class, is);

        // calling hasNext() multiple times before next() must not advance the cursor
        assertTrue(reader.hasNext());
        assertTrue(reader.hasNext());
        assertTrue(reader.hasNext());
        assertNotNull(reader.next());
        assertFalse(reader.hasNext());
        reader.close();
    }
}
