package com.joutvhu.fixedwidth.parser.exception;

public class FindTypeException extends FixedException {
    private static final long serialVersionUID = 6135055750143821303L;

    private final Class<?> classType;

    public FindTypeException(String message, Class<?> classType) {
        super(message);
        this.classType = classType;
    }

    public FindTypeException(String message, Throwable cause, Class<?> classType) {
        super(message, cause);
        this.classType = classType;
    }

    public FindTypeException(Throwable cause, Class<?> classType) {
        super(cause);
        this.classType = classType;
    }
}
