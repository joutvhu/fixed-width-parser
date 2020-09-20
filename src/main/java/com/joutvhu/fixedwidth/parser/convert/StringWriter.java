package com.joutvhu.fixedwidth.parser.convert;

/**
 * Serialization
 * Fixed width string writer
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface StringWriter<T> {
    /**
     * Serialize
     * Write object to string
     *
     * @param value object
     * @return string
     */
    String write(T value);
}
