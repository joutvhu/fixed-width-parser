package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when the actual line length does not match the expected length.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class LengthMismatchException extends FixedValidationException {
    private static final long serialVersionUID = -3654128954785214698L;

    public LengthMismatchException(String message) {
        super(message);
    }
}
