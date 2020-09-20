package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.StringAssembler;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public interface StringReader<T> {
    T read(StringAssembler assembler);
}
