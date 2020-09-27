package com.joutvhu.fixedwidth.parser.support;

/**
 * Fixed width string serialization strategy
 *
 * @author Giao Ho
 * @since 1.1.0
 */
public interface WriteStrategy {
    String write(FixedTypeInfo info, Object value);
}
