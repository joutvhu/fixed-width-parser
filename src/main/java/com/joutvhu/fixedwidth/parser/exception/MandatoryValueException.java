package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when a required field is blank or null.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class MandatoryValueException extends FixedValidationException {
    private static final long serialVersionUID = 8965412354789632145L;

    public MandatoryValueException(String message) {
        super(message);
    }
}
