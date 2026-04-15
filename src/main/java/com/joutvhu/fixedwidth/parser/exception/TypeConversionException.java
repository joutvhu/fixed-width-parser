package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when data cannot be converted to the target type.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class TypeConversionException extends FixedValidationException {
    private static final long serialVersionUID = -4587123654789632541L;

    public TypeConversionException(String message) {
        super(message);
    }

    public TypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
