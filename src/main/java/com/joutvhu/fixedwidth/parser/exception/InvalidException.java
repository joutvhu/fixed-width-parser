package com.joutvhu.fixedwidth.parser.exception;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class InvalidException extends FixedException {
    private static final long serialVersionUID = -1609951563426857790L;

    public InvalidException(String message) {
        super(message);
    }

    public InvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}