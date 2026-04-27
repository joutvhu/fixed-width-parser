package com.joutvhu.fixedwidth.parser.support;

import java.util.List;

/**
 * Accumulates field parts during a write operation and assembles them into
 * the final fixed-width output string.
 *
 * <p>Unlike {@link StringAssembler} (which operates at the raw character level),
 * {@code FixedStringBuilder} works with named parts and defers the final
 * concatenation until {@link #build()} is called.  This allows handlers to
 * inspect or modify already-written fields before the output is finalised.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public interface FixedStringBuilder {

    /**
     * Creates a new, empty builder.
     */
    static FixedStringBuilder create() {
        return new DefaultFixedStringBuilder();
    }

    // ── Mutating ─────────────────────────────────────────────────────────────

    /**
     * Adds a field part.
     *
     * @param fieldName the field name (used as key for later retrieval)
     * @param info      field metadata ({@code null} is allowed for ad-hoc parts)
     * @param value     the padded string value to store
     */
    FixedStringBuilder addPart(String fieldName, FixedTypeInfo info, String value);

    /**
     * Replaces the value of an already-added part.
     *
     * @throws IllegalArgumentException if no part with {@code fieldName} exists
     */
    FixedStringBuilder replacePart(String fieldName, String newValue);

    // ── Querying ─────────────────────────────────────────────────────────────

    /**
     * Returns the value of the named part, or {@code null} if not yet added.
     */
    String getPart(String fieldName);

    /**
     * Returns {@code true} if a part with the given name has been added.
     */
    boolean hasPart(String fieldName);

    /**
     * All parts in the order they were added.
     */
    List<BuiltPart> getAllParts();

    /**
     * Same as {@link #getAllParts()} — provided for semantic clarity in handlers.
     */
    List<BuiltPart> getCompletedParts();

    // ── Building ─────────────────────────────────────────────────────────────

    /**
     * Assembles all parts into the final output string by placing each part's
     * value at its {@code start} position according to the associated
     * {@link FixedTypeInfo}.
     */
    String build();
}
