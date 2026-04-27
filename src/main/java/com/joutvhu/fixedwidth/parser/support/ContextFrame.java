package com.joutvhu.fixedwidth.parser.support;

/**
 * Represents one node in the parse/export tree at a given point in time.
 *
 * <p>A new frame is pushed onto the {@link ParseContext} stack whenever the
 * engine enters an object, field, collection item, or map entry, and popped
 * when it leaves.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public interface ContextFrame {

    // ── Identity ─────────────────────────────────────────────────────────────

    /** Metadata for the type/field this frame represents. */
    FixedTypeInfo getTypeInfo();

    /** What kind of node this frame is. */
    FrameType getFrameType();

    /** Depth in the tree — root object is 0, its fields are 1, etc. */
    int getDepth();

    /**
     * Index within a collection or map; {@code -1} when not applicable.
     */
    int getIndex();

    // ── String values ────────────────────────────────────────────────────────

    /**
     * The raw substring extracted by {@code start/length} slicing.
     * Immutable — never changes after {@link Phase#READ_AFTER_CUT}.
     * {@code null} during WRITE phases.
     */
    String getRawString();

    /**
     * The string after padding/trim handlers have run.
     * Starts equal to {@link #getRawString()} and may be modified by handlers
     * at {@link Phase#READ_AFTER_CUT} or {@link Phase#READ_AFTER_TRANSFORM}.
     */
    String getProcessedString();

    /** Replaces the processed string. Only meaningful during READ phases. */
    void setProcessedString(String value);

    // ── Object being built ───────────────────────────────────────────────────

    /**
     * The object being assembled during READ, or the source object during WRITE.
     * During READ, only fields parsed so far are populated.
     */
    Object getPartialResult();

    /** The low-level string assembler for this frame. */
    StringAssembler getAssembler();

    /**
     * The write builder for this frame's object.
     * Non-null only during WRITE phases on OBJECT frames.
     * {@code null} during READ phases and on non-OBJECT frames.
     */
    FixedStringBuilder getBuilder();
}
