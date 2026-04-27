package com.joutvhu.fixedwidth.parser.doc;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates human-readable documentation for a {@code @FixedObject}-annotated class.
 *
 * <p>Supported output formats: Markdown table, JSON, CSV.
 *
 * <p>Usage:
 * <pre>{@code
 * String md  = SchemaDocument.of(Product.class).toMarkdown();
 * String json = SchemaDocument.of(Product.class).toJson();
 * String csv  = SchemaDocument.of(Product.class).toCsv();
 * }</pre>
 *
 * @author Giao Ho
 * @since 1.7.0
 */
public class SchemaDocument {

    /** One row in the schema table. */
    public static final class FieldRow {
        private final String name;
        private final int start;
        private final int length;
        private final String type;
        private final boolean required;
        private final String padding;
        private final String alignment;

        FieldRow(FixedTypeInfo info) {
            this.name = info.getName();
            this.start = info.getStart();
            this.length = info.getLength();
            this.type = info.getType().getSimpleName();
            this.required = info.isRequire();
            this.padding = info.getPadding() != null ? String.valueOf(info.getPadding()) : "";
            this.alignment = info.getAlignment() != null ? info.getAlignment().name() : "";
        }

        public String getName()      { return name; }
        public int    getStart()     { return start; }
        public int    getLength()    { return length; }
        public String getType()      { return type; }
        public boolean isRequired()  { return required; }
        public String getPadding()   { return padding; }
        public String getAlignment() { return alignment; }
    }

    private final Class<?> rootType;
    private final List<FieldRow> rows;

    private SchemaDocument(Class<?> type) {
        this.rootType = type;
        this.rows = buildRows(FixedTypeInfo.of(type));
    }

    /** Creates a {@link SchemaDocument} for the given class. */
    public static SchemaDocument of(Class<?> type) {
        return new SchemaDocument(type);
    }

    /** Returns the field rows in declaration order. */
    public List<FieldRow> getRows() {
        return new ArrayList<>(rows);
    }

    // ── Output formats ────────────────────────────────────────────────────────

    /**
     * Renders the schema as a Markdown table.
     *
     * <pre>
     * ## Product
     * | Field | Start | Length | Type | Required | Padding | Alignment |
     * |-------|-------|--------|------|----------|---------|-----------|
     * | id    | 0     | 5      | Long | true     |         | RIGHT     |
     * </pre>
     */
    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("## ").append(rootType.getSimpleName()).append("\n\n");
        sb.append("| Field | Start | Length | Type | Required | Padding | Alignment |\n");
        sb.append("|-------|-------|--------|------|----------|---------|-----------|\n");
        for (FieldRow row : rows) {
            sb.append("| ").append(row.name)
              .append(" | ").append(row.start)
              .append(" | ").append(row.length)
              .append(" | ").append(row.type)
              .append(" | ").append(row.required)
              .append(" | ").append(escapeMd(row.padding))
              .append(" | ").append(row.alignment)
              .append(" |\n");
        }
        return sb.toString();
    }

    /**
     * Renders the schema as a JSON array.
     *
     * <pre>
     * {"class":"Product","fields":[{"name":"id","start":0,"length":5,...}]}
     * </pre>
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"class\":\"").append(rootType.getSimpleName()).append("\",\"fields\":[");
        for (int i = 0; i < rows.size(); i++) {
            FieldRow row = rows.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"name\":\"").append(jsonEscape(row.name)).append("\",")
              .append("\"start\":").append(row.start).append(",")
              .append("\"length\":").append(row.length).append(",")
              .append("\"type\":\"").append(jsonEscape(row.type)).append("\",")
              .append("\"required\":").append(row.required).append(",")
              .append("\"padding\":\"").append(jsonEscape(row.padding)).append("\",")
              .append("\"alignment\":\"").append(jsonEscape(row.alignment)).append("\"")
              .append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * Renders the schema as CSV (comma-separated values).
     *
     * <pre>
     * name,start,length,type,required,padding,alignment
     * id,0,5,Long,true,,RIGHT
     * </pre>
     */
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("name,start,length,type,required,padding,alignment\n");
        for (FieldRow row : rows) {
            sb.append(csvEscape(row.name)).append(",")
              .append(row.start).append(",")
              .append(row.length).append(",")
              .append(csvEscape(row.type)).append(",")
              .append(row.required).append(",")
              .append(csvEscape(row.padding)).append(",")
              .append(csvEscape(row.alignment)).append("\n");
        }
        return sb.toString();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static List<FieldRow> buildRows(FixedTypeInfo typeInfo) {
        List<FieldRow> rows = new ArrayList<>();
        for (FixedTypeInfo field : typeInfo.getElementTypeInfo()) {
            rows.add(new FieldRow(field));
        }
        return rows;
    }

    private static String escapeMd(String s) {
        if (s == null || s.isEmpty()) return "";
        // Escape pipe characters in Markdown tables
        return s.replace("|", "\\|");
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String csvEscape(String s) {
        if (s == null || s.isEmpty()) return "";
        // Wrap in quotes if contains comma, quote, or newline
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
