package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when a class is missing the required @FixedObject annotation.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class MissingAnnotationException extends FixedParserException {
    private static final long serialVersionUID = 4781236549215874263L;

    public MissingAnnotationException(String message) {
        super(message);
    }
}
