package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when fields within a fixed-width object overlap in their defined positions.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FieldOverlapException extends FixedParserException {
    private static final long serialVersionUID = -3654128954721369854L;

    public FieldOverlapException(String message) {
        super(message);
    }
}
