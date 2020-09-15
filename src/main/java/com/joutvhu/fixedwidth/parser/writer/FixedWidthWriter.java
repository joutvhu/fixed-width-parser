package com.joutvhu.fixedwidth.parser.writer;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FixedWidthWriter<T> {
    protected FixedTypeInfo info;

    public FixedWidthWriter(FixedTypeInfo info) {
        this.info = info;
    }

    protected void skip() {
        throw new FixedException("Can't use this writer.");
    }

    public abstract String write(T value);
}
