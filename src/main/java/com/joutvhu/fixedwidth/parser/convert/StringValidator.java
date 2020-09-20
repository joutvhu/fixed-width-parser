package com.joutvhu.fixedwidth.parser.convert;

/**
 * Fixed width validator
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface StringValidator {
    /**
     * Validate string value
     *
     * @param value is string value
     * @param type  the {@link ValidationType}
     */
    void validate(String value, ValidationType type);
}
