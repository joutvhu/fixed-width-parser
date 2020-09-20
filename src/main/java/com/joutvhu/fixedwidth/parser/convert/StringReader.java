package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.StringAssembler;

/**
 * Deserialization
 * Fixed width string reader
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface StringReader<T> {
    /**
     * Deserialize
     * Read object value from {@link StringAssembler}
     *
     * @param assembler the {@link StringAssembler}
     * @return object value
     */
    T read(StringAssembler assembler);
}
