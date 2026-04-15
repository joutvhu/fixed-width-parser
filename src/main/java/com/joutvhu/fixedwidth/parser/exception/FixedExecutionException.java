package com.joutvhu.fixedwidth.parser.exception;

/**
 * Exception for errors occurring during the execution of parsing or exporting.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FixedExecutionException extends FixedException {
    private static final long serialVersionUID = 5894123654789512365L;

    public FixedExecutionException(String message) {
        super(message);
    }

    public FixedExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
