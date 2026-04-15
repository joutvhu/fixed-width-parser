package com.joutvhu.fixedwidth.parser.exception;

/**
 * Exception for errors related to data validation during parsing or exporting.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FixedValidationException extends FixedExecutionException {
    private static final long serialVersionUID = -7841253698547123654L;

    public FixedValidationException(String message) {
        super(message);
    }

    public FixedValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
