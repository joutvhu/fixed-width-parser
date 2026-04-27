package com.joutvhu.fixedwidth.parser.support;

/**
 * Represents a specific point in the read or write pipeline.
 *
 * <p>READ phases (prefix {@code READ_}) occur during {@link FixedParser#parse}:
 * <ol>
 *   <li>{@link #READ_PRE_CUT} — before slicing the raw string</li>
 *   <li>{@link #READ_AFTER_CUT} — after slicing, raw string available</li>
 *   <li>{@link #READ_AFTER_TRANSFORM} — after padding/trim handlers run</li>
 *   <li>{@link #READ_AFTER_CONVERT} — after conversion to Java type</li>
 *   <li>{@link #READ_AFTER_SET} — after value is set on the object</li>
 *   <li>{@link #READ_AFTER_OBJECT} — after all fields of an object are read</li>
 * </ol>
 *
 * <p>WRITE phases (prefix {@code WRITE_}) occur during {@link FixedParser#export}:
 * <ol>
 *   <li>{@link #WRITE_PRE_GET} — before reading the field value from the object</li>
 *   <li>{@link #WRITE_AFTER_GET} — after reading the field value</li>
 *   <li>{@link #WRITE_AFTER_CONVERT} — after converting the value to string</li>
 *   <li>{@link #WRITE_AFTER_TRANSFORM} — after padding/alignment applied</li>
 *   <li>{@link #WRITE_AFTER_PUT} — after the string is placed into the output</li>
 *   <li>{@link #WRITE_AFTER_OBJECT} — after all fields of an object are written</li>
 * </ol>
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public enum Phase {

    // ── READ pipeline ────────────────────────────────────────────────────────

    /** Before slicing the input string. Context has assembler but no raw string yet. */
    READ_PRE_CUT,

    /**
     * After slicing according to {@code start/length}. Raw string is available via
     * {@link ParseContext#getRawString()} and is immutable for the rest of the session.
     */
    READ_AFTER_CUT,

    /**
     * After padding/trim handlers have run. Processed string is available via
     * {@link ParseContext#getProcessedString()}.
     */
    READ_AFTER_TRANSFORM,

    /** After the processed string has been converted to a Java value. */
    READ_AFTER_CONVERT,

    /** After the Java value has been set on the parent object. */
    READ_AFTER_SET,

    /** After all fields of an object have been read. Object-level handlers run here. */
    READ_AFTER_OBJECT,

    // ── WRITE pipeline ───────────────────────────────────────────────────────

    /** Before reading the field value from the object. Handlers may inject a computed value. */
    WRITE_PRE_GET,

    /** After reading the field value from the object. Handlers may transform it (e.g. encrypt). */
    WRITE_AFTER_GET,

    /** After the Java value has been converted to a string. */
    WRITE_AFTER_CONVERT,

    /** After padding/alignment has been applied. Final string form before output. */
    WRITE_AFTER_TRANSFORM,

    /** After the padded string has been placed into the output assembler/builder. */
    WRITE_AFTER_PUT,

    /** After all fields of an object have been written. Object-level handlers run here. */
    WRITE_AFTER_OBJECT;

    /** Returns {@code true} if this phase belongs to the read pipeline. */
    public boolean isRead() {
        return name().startsWith("READ_");
    }

    /** Returns {@code true} if this phase belongs to the write pipeline. */
    public boolean isWrite() {
        return name().startsWith("WRITE_");
    }
}
