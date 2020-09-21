package com.joutvhu.fixedwidth.parser.exception;

public class ParserException extends RuntimeException {
    private static final long serialVersionUID = -8494256608109253120L;

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }
}
