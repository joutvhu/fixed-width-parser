package com.joutvhu.fixedwidth.parser.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link FixedStringBuilder}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class DefaultFixedStringBuilder implements FixedStringBuilder {

    private final Map<String, DefaultBuiltPart> parts = new LinkedHashMap<>();

    @Override
    public FixedStringBuilder addPart(String fieldName, FixedTypeInfo info, String value) {
        parts.put(fieldName, new DefaultBuiltPart(fieldName, info, value));
        return this;
    }

    @Override
    public FixedStringBuilder replacePart(String fieldName, String newValue) {
        DefaultBuiltPart existing = parts.get(fieldName);
        if (existing == null)
            throw new IllegalArgumentException("No part found for field: " + fieldName);
        parts.put(fieldName, new DefaultBuiltPart(fieldName, existing.getTypeInfo(), newValue));
        return this;
    }

    @Override
    public String getPart(String fieldName) {
        DefaultBuiltPart part = parts.get(fieldName);
        return part != null ? part.getValue() : null;
    }

    @Override
    public boolean hasPart(String fieldName) {
        return parts.containsKey(fieldName);
    }

    @Override
    public List<BuiltPart> getAllParts() {
        return Collections.unmodifiableList(new ArrayList<>(parts.values()));
    }

    @Override
    public List<BuiltPart> getCompletedParts() {
        return getAllParts();
    }

    @Override
    public String build() {
        if (parts.isEmpty()) return "";

        // Determine total length from the rightmost end of all parts
        int totalLength = 0;
        for (DefaultBuiltPart part : parts.values()) {
            FixedTypeInfo info = part.getTypeInfo();
            if (info != null && info.getStart() != null && info.getLength() != null
                && info.getLength() > 0) {
                totalLength = Math.max(totalLength, info.getStart() + info.getLength());
            }
        }

        // Use StringAssembler to place each part at its position
        StringAssembler assembler = FixedStringAssembler.instance();
        for (DefaultBuiltPart part : parts.values()) {
            FixedTypeInfo info = part.getTypeInfo();
            if (info != null && info.getStart() != null) {
                assembler.set(info.getStart(), info.getLength(), part.getValue());
            } else {
                // No position info — append sequentially
                int pos = assembler.length();
                assembler.set(pos, null, part.getValue());
            }
        }
        return assembler.getValue();
    }

    // ── Inner class ──────────────────────────────────────────────────────────

    private static class DefaultBuiltPart implements BuiltPart {
        private final String fieldName;
        private final FixedTypeInfo typeInfo;
        private final String value;

        DefaultBuiltPart(String fieldName, FixedTypeInfo typeInfo, String value) {
            this.fieldName = fieldName;
            this.typeInfo = typeInfo;
            this.value = value;
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public FixedTypeInfo getTypeInfo() {
            return typeInfo;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getRawValue() {
            return value;
        } // Phase 4 will refine this
    }
}
