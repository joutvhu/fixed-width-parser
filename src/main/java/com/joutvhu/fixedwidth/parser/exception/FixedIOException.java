package com.joutvhu.fixedwidth.parser.exception;

import java.io.IOException;

/**
 * Exception for errors related to I/O operations.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FixedIOException extends FixedException {
    private static final long serialVersionUID = -4587123654789632541L;

    public FixedIOException(String message) {
        super(message);
    }

    public FixedIOException(String message, IOException cause) {
        super(message, cause);
    }
}
