package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.support.Phase;

/**
 * Describes a single error that occurred during a parse or export operation
 * when running in collect-all-errors mode.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public interface ParseError {

    /**
     * Human-readable description of the error.
     */
    String getMessage();

    /**
     * The pipeline phase at which the error occurred, e.g.
     * {@link Phase#READ_AFTER_TRANSFORM}.
     */
    Phase getPhase();

    /**
     * Dot-separated path to the field that caused the error, e.g.
     * {@code "Product.items[2].price"}.
     */
    String getFieldPath();

    /**
     * The raw string value (after slicing, before any transformation) at the
     * time the error occurred.
     */
    String getRawValue();

    /**
     * The underlying exception that triggered this error.
     */
    Throwable getCause();
}
