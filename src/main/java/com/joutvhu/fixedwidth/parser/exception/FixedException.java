package com.joutvhu.fixedwidth.parser.exception;

public class FixedException extends RuntimeException {
    private static final long serialVersionUID = -5641430689223945394L;

    public FixedException() {
        super();
    }

    public FixedException(String message) {
        super(message);
    }

    public FixedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FixedException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public FixedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
