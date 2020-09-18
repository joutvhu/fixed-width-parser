package com.joutvhu.fixedwidth.parser.handler;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FixedWidthHandler {
    protected FixedTypeInfo info;
    protected FixedParseStrategy strategy;

    public FixedWidthHandler(FixedTypeInfo info, FixedParseStrategy strategy) {
        this.info = info;
        this.strategy = strategy;
    }

    protected void skip() {
        throw new FixedException("Can't use this class.");
    }
}
