package com.joutvhu.fixedwidth.parser.validation;

import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;
import com.joutvhu.fixedwidth.parser.annotation.FixedCount;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.exception.CircularDependencyException;
import com.joutvhu.fixedwidth.parser.exception.FieldOverlapException;
import com.joutvhu.fixedwidth.parser.exception.FixedParserException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the schema of a {@link FixedObject}-annotated class at startup time,
 * before any parsing occurs.
 *
 * <p>Detects:
 * <ul>
 *   <li>Field overlap — two fields whose {@code start/length} ranges overlap</li>
 *   <li>Missing dependency field — {@code @FixedConditional} or {@code @FixedCount}
 *       references a field name that does not exist on the class</li>
 *   <li>Circular subtype reference — a subtype that directly or transitively
 *       references itself</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * FixedParser.parser().validate(Product.class).throwIfInvalid();
 * }</pre>
 *
 * @author Giao Ho
 * @since 1.7.0
 */
public class SchemaValidator {

    private final List<String> errors = new ArrayList<>();

    private SchemaValidator() {}

    /**
     * Validates the given class and returns a {@link SchemaValidator} whose
     * result can be inspected or thrown.
     */
    public static SchemaValidator validate(Class<?> type) {
        SchemaValidator v = new SchemaValidator();
        v.validateClass(type, new HashSet<>());
        return v;
    }

    // ── Public result API ─────────────────────────────────────────────────────

    /** Returns {@code true} if no schema errors were found. */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /** Returns all error messages found during validation. */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Throws a {@link FixedParserException} if any schema errors were found.
     *
     * @return this (fluent, only reached when valid)
     */
    public SchemaValidator throwIfInvalid() {
        if (!errors.isEmpty()) {
            throw new FixedParserException(
                    "Schema validation failed for fixed-width class:\n  - " +
                    String.join("\n  - ", errors));
        }
        return this;
    }

    // ── Internal validation ───────────────────────────────────────────────────

    private void validateClass(Class<?> type, Set<Class<?>> visiting) {
        if (type == null || type == void.class) return;
        if (visiting.contains(type)) {
            errors.add("Circular subtype reference detected for: " + type.getName());
            return;
        }
        visiting.add(type);

        FixedTypeInfo typeInfo = FixedTypeInfo.of(type);
        List<FixedTypeInfo> fields = typeInfo.getElementTypeInfo();

        checkFieldOverlap(type, fields);
        checkMissingDependencyFields(type, fields);
        checkSubtypes(type, typeInfo, visiting);

        // Recurse into nested object fields
        for (FixedTypeInfo field : fields) {
            if (field.getFixedObject() != null && !field.getElementTypeInfo().isEmpty()) {
                validateClass(field.getType(), new HashSet<>(visiting));
            }
        }

        visiting.remove(type);
    }

    /**
     * Checks that no two fields have overlapping {@code start/length} ranges.
     * Fields with {@code length == 0} (unlimited) are skipped.
     */
    private void checkFieldOverlap(Class<?> type, List<FixedTypeInfo> fields) {
        for (int i = 0; i < fields.size(); i++) {
            FixedTypeInfo a = fields.get(i);
            if (a.getLength() == 0) continue;
            int aEnd = a.getStart() + a.getLength();

            for (int j = i + 1; j < fields.size(); j++) {
                FixedTypeInfo b = fields.get(j);
                if (b.getLength() == 0) continue;
                int bEnd = b.getStart() + b.getLength();

                // Overlap when ranges intersect: a.start < bEnd && b.start < aEnd
                if (a.getStart() < bEnd && b.getStart() < aEnd) {
                    errors.add(String.format(
                            "[%s] Fields '%s' (start=%d, length=%d) and '%s' (start=%d, length=%d) overlap.",
                            type.getSimpleName(),
                            a.getName(), a.getStart(), a.getLength(),
                            b.getName(), b.getStart(), b.getLength()));
                }
            }
        }
    }

    /**
     * Checks that fields referenced by {@code @FixedConditional(dependsOnField)}
     * and {@code @FixedCount(field)} actually exist on the class.
     */
    private void checkMissingDependencyFields(Class<?> type, List<FixedTypeInfo> fields) {
        Set<String> fieldNames = new HashSet<>();
        for (FixedTypeInfo f : fields) {
            fieldNames.add(f.getName());
        }

        for (FixedTypeInfo f : fields) {
            if (f.getField() == null) continue;

            FixedConditional cond = f.getField().getAnnotation(FixedConditional.class);
            if (cond != null && !cond.dependsOnField().isEmpty()) {
                if (!fieldNames.contains(cond.dependsOnField())) {
                    errors.add(String.format(
                            "[%s] Field '%s' has @FixedConditional(dependsOnField=\"%s\") " +
                            "but no such field exists.",
                            type.getSimpleName(), f.getName(), cond.dependsOnField()));
                }
            }

            FixedCount count = f.getField().getAnnotation(FixedCount.class);
            if (count != null && !count.field().isEmpty()) {
                if (!fieldNames.contains(count.field())) {
                    errors.add(String.format(
                            "[%s] Field '%s' has @FixedCount(field=\"%s\") " +
                            "but no such field exists.",
                            type.getSimpleName(), f.getName(), count.field()));
                }
            }
        }
    }

    /**
     * Checks that subtypes declared in {@code @FixedObject(subTypes)} are
     * actual subclasses of the parent type, and detects circular subtype chains.
     */
    private void checkSubtypes(Class<?> type, FixedTypeInfo typeInfo, Set<Class<?>> visiting) {
        FixedObject fixedObject = typeInfo.getFixedObject();
        if (fixedObject == null) return;

        for (FixedObject.Type subTypeDef : fixedObject.subTypes()) {
            Class<?> subType = subTypeDef.value();
            if (subType == null || subType == void.class) continue;

            if (!type.isAssignableFrom(subType)) {
                errors.add(String.format(
                        "[%s] Subtype '%s' is not a subclass of '%s'.",
                        type.getSimpleName(), subType.getSimpleName(), type.getSimpleName()));
            }

            // Detect circular subtype reference
            if (visiting.contains(subType)) {
                errors.add(String.format(
                        "[%s] Circular subtype reference: '%s' is already being validated.",
                        type.getSimpleName(), subType.getSimpleName()));
            }
        }
    }
}
