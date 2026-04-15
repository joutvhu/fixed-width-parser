package com.joutvhu.fixedwidth.parser.exception;

/**
 * Exception for parser configuration errors.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FixedParserException extends FixedException {
    private static final long serialVersionUID = -2451368426814952671L;

    public FixedParserException(String message) {
        super(message);
    }

    public FixedParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
