package com.joutvhu.fixedwidth.parser.writer;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FixedWidthWriter<T> {
    protected FixedTypeInfo info;
    protected FixedParseStrategy strategy;

    public FixedWidthWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        this.info = info;
        this.strategy = strategy;
    }

    protected void skip() {
        throw new FixedException("Can't use this writer.");
    }

    public abstract String write(T value);
}
