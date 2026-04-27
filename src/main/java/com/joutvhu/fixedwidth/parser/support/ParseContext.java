package com.joutvhu.fixedwidth.parser.support;

import java.util.List;

/**
 * Carries state for a single parse or export call.
 *
 * <p>A new {@code ParseContext} is created for every call to
 * {@link com.joutvhu.fixedwidth.parser.FixedParser#parse} or
 * {@link com.joutvhu.fixedwidth.parser.FixedParser#export} and is never shared
 * between calls.
 *
 * <p>The context maintains a stack of {@link ContextFrame}s that mirrors the
 * object tree being traversed.  Handlers can read and write properties at two
 * scopes:
 * <ul>
 *   <li><b>scoped</b> — cleared automatically when the current frame is popped</li>
 *   <li><b>global</b> — lives for the entire parse/export session</li>
 * </ul>
 *
 * <p>Properties set via {@link #put}/{@link #putGlobal} are global.
 * Use {@link #putScoped} for frame-local data.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public interface ParseContext {

    // ── Phase ────────────────────────────────────────────────────────────────

    /**
     * The phase currently executing.
     * The {@code READ_} / {@code WRITE_} prefix encodes the direction, so no
     * separate {@code getDirection()} method is needed.
     */
    Phase getPhase();

    // ── Frame stack ──────────────────────────────────────────────────────────

    /**
     * The frame for the node currently being processed.
     */
    ContextFrame currentFrame();

    /**
     * The frame for the parent node, or {@code null} when at the root.
     */
    ContextFrame parentFrame();

    /**
     * The full stack from root (index 0) to the current frame (last index).
     * Returns an unmodifiable view.
     */
    List<ContextFrame> frameStack();

    // ── Convenience delegates to currentFrame() ──────────────────────────────

    /**
     * Raw string of the current frame — delegates to
     * {@link ContextFrame#getRawString()}.
     */
    default String getRawString() {
        ContextFrame f = currentFrame();
        return f != null ? f.getRawString() : null;
    }

    /**
     * Processed string of the current frame — delegates to
     * {@link ContextFrame#getProcessedString()}.
     */
    default String getProcessedString() {
        ContextFrame f = currentFrame();
        return f != null ? f.getProcessedString() : null;
    }

    /**
     * Replaces the processed string on the current frame — delegates to
     * {@link ContextFrame#setProcessedString(String)}.
     */
    default void setProcessedString(String value) {
        ContextFrame f = currentFrame();
        if (f != null) f.setProcessedString(value);
    }

    // ── Current value ────────────────────────────────────────────────────────

    /**
     * The Java value currently being read or written.
     * During READ: set after conversion; handlers at {@link Phase#READ_AFTER_CONVERT}
     * and later may read it.
     * During WRITE: the value retrieved from the object field.
     */
    Object getCurrentValue();

    /**
     * Replaces the current value.  Handlers may call this to transform the
     * value before it is used by the next step in the pipeline.
     */
    void setCurrentValue(Object value);

    /**
     * Signals that the current field should be skipped (set to {@code null} /
     * default and not written to output).  Useful for conditional fields.
     */
    void skipCurrentField();

    // ── Properties ───────────────────────────────────────────────────────────

    /**
     * Stores a global property that persists for the entire session.
     * Equivalent to {@link #putGlobal(String, Object)}.
     */
    void put(String key, Object value);

    /**
     * Stores a global property.
     */
    void putGlobal(String key, Object value);

    /**
     * Stores a scoped property that is automatically removed when the current
     * frame is popped.
     */
    void putScoped(String key, Object value);

    /**
     * Returns the property value cast to {@code type}, or {@code null} if absent.
     */
    <T> T get(String key, Class<T> type);

    /**
     * Returns {@code true} if a property with the given key exists.
     */
    boolean has(String key);

    /**
     * Removes a property.
     */
    void remove(String key);

    // ── Parser / session properties ──────────────────────────────────────────

    /**
     * Reads a property following the priority chain:
     * session property → parser config → {@code defaultValue}.
     *
     * @param key          property key
     * @param type         expected type
     * @param defaultValue returned when the property is not found at any level
     */
    <T> T getProperty(String key, Class<T> type, T defaultValue);

    /**
     * Reads a property following the priority chain.
     * Throws {@link IllegalStateException} if the property is not found at any level.
     */
    <T> T requireProperty(String key, Class<T> type);
}
