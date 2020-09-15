package com.joutvhu.fixedwidth.parser.reader;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

public abstract class FixedWidthReader<T> {
    protected FixedTypeInfo info;
    protected FixedParseStrategy strategy;

    public FixedWidthReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        this.info = info;
        this.strategy = strategy;
    }

    protected void skip() {
        throw new FixedException("Can't use this reader.");
    }

    public abstract T read(StringAssembler assembler);
}
