package com.joutvhu.fixedwidth.parser.exception;

/**
 * Thrown when the total length of the defined fields does not match the expected length.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class InconsistentLengthException extends FixedParserException {
    private static final long serialVersionUID = 1452369874521632547L;

    public InconsistentLengthException(String message) {
        super(message);
    }
}
