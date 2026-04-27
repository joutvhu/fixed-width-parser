package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.support.Phase;

/**
 * Default implementation of {@link ParseError}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class DefaultParseError implements ParseError {

    private final String message;
    private final Phase phase;
    private final String fieldPath;
    private final String rawValue;
    private final Throwable cause;

    public DefaultParseError(String message, Phase phase, String fieldPath,
                             String rawValue, Throwable cause) {
        this.message = message;
        this.phase = phase;
        this.fieldPath = fieldPath;
        this.rawValue = rawValue;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Phase getPhase() {
        return phase;
    }

    @Override
    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public String getRawValue() {
        return rawValue;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
