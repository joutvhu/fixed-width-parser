package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when a circular field dependency is detected during metadata build.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class CircularDependencyException extends FixedParserException {
    private static final long serialVersionUID = 1L;

    public CircularDependencyException(String message) {
        super(message);
    }
}
