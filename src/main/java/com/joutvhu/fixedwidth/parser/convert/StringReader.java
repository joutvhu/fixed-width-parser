package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.StringAssembler;

public interface StringReader<T> {
    T read(StringAssembler assembler);
}
