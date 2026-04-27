package com.joutvhu.fixedwidth.parser.phase6;

import com.joutvhu.fixedwidth.parser.FixedParser;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.doc.SchemaDocument;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6 — Schema documentation generation
 */
class SchemaDocumentTest {

    @FixedObject
    @Data
    @NoArgsConstructor
    public static class ProductModel {
        @FixedField(start = 0, length = 5)
        private Long id;

        @FixedField(start = 5, length = 20)
        private String name;

        @FixedField(start = 25, length = 10)
        private Integer price;
    }

    // ── Rows ──────────────────────────────────────────────────────────────────

    @Test
    void getRows_returnsAllFields() {
        SchemaDocument doc = SchemaDocument.of(ProductModel.class);
        List<SchemaDocument.FieldRow> rows = doc.getRows();
        assertEquals(3, rows.size());
    }

    @Test
    void getRows_correctFieldMetadata() {
        SchemaDocument doc = SchemaDocument.of(ProductModel.class);
        SchemaDocument.FieldRow idRow = doc.getRows().get(0);
        assertEquals("id", idRow.getName());
        assertEquals(0, idRow.getStart());
        assertEquals(5, idRow.getLength());
        assertEquals("Long", idRow.getType());
    }

    // ── Markdown ──────────────────────────────────────────────────────────────

    @Test
    void toMarkdown_containsClassName() {
        String md = SchemaDocument.of(ProductModel.class).toMarkdown();
        assertTrue(md.contains("ProductModel"), "Markdown should contain class name");
    }

    @Test
    void toMarkdown_containsFieldNames() {
        String md = SchemaDocument.of(ProductModel.class).toMarkdown();
        assertTrue(md.contains("id"));
        assertTrue(md.contains("name"));
        assertTrue(md.contains("price"));
    }

    @Test
    void toMarkdown_hasTableHeader() {
        String md = SchemaDocument.of(ProductModel.class).toMarkdown();
        assertTrue(md.contains("| Field |"), "Should have table header");
        assertTrue(md.contains("|----"), "Should have separator row");
    }

    @Test
    void toMarkdown_containsStartAndLength() {
        String md = SchemaDocument.of(ProductModel.class).toMarkdown();
        assertTrue(md.contains("| 0 |") || md.contains("| 0|"),
                "Should contain start=0");
        assertTrue(md.contains("| 5 |") || md.contains("| 5|"),
                "Should contain length=5");
    }

    // ── JSON ──────────────────────────────────────────────────────────────────

    @Test
    void toJson_isValidJsonStructure() {
        String json = SchemaDocument.of(ProductModel.class).toJson();
        assertTrue(json.startsWith("{"), "Should start with {");
        assertTrue(json.endsWith("}"), "Should end with }");
        assertTrue(json.contains("\"fields\":["), "Should have fields array");
    }

    @Test
    void toJson_containsClassName() {
        String json = SchemaDocument.of(ProductModel.class).toJson();
        assertTrue(json.contains("\"class\":\"ProductModel\""));
    }

    @Test
    void toJson_containsFieldData() {
        String json = SchemaDocument.of(ProductModel.class).toJson();
        assertTrue(json.contains("\"name\":\"id\""));
        assertTrue(json.contains("\"start\":0"));
        assertTrue(json.contains("\"length\":5"));
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    @Test
    void toCsv_hasHeaderRow() {
        String csv = SchemaDocument.of(ProductModel.class).toCsv();
        String firstLine = csv.split("\n")[0];
        assertEquals("name,start,length,type,required,padding,alignment", firstLine);
    }

    @Test
    void toCsv_hasCorrectRowCount() {
        String csv = SchemaDocument.of(ProductModel.class).toCsv();
        String[] lines = csv.split("\n");
        // 1 header + 3 fields
        assertEquals(4, lines.length);
    }

    @Test
    void toCsv_firstDataRow_correctValues() {
        String csv = SchemaDocument.of(ProductModel.class).toCsv();
        String dataRow = csv.split("\n")[1];
        assertTrue(dataRow.startsWith("id,0,5,Long,"),
                "First data row should start with id,0,5,Long, but was: " + dataRow);
    }

    // ── FixedParser integration ───────────────────────────────────────────────

    @Test
    void fixedParser_document_returnsSchemaDocument() {
        SchemaDocument doc = FixedParser.parser().document(ProductModel.class);
        assertNotNull(doc);
        assertEquals(3, doc.getRows().size());
    }
}
