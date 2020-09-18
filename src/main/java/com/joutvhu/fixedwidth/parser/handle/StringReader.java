package com.joutvhu.fixedwidth.parser.handle;

import com.joutvhu.fixedwidth.parser.support.StringAssembler;

public interface StringReader<T> {
    T read(StringAssembler assembler);
}
