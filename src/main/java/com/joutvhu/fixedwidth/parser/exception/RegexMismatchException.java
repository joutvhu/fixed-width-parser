package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when the data does not match the required regular expression pattern.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class RegexMismatchException extends FixedValidationException {
    private static final long serialVersionUID = -2145876325412589632L;

    public RegexMismatchException(String message) {
        super(message);
    }
}
