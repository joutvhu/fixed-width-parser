package com.joutvhu.fixedwidth.parser.domain;

/**
 * Strategy applied when a field-level error occurs during parsing.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public enum OnError {

    /** Re-throw the exception immediately (default / fail-fast behaviour). */
    THROW,

    /** Set the field to {@code null} and continue parsing. */
    NULL,

    /**
     * Set the field to the value specified by the annotation's
     * {@code defaultValue} attribute and continue parsing.
     */
    DEFAULT_VALUE,

    /** Skip the field entirely (leave it at its Java default) and continue. */
    SKIP
}
