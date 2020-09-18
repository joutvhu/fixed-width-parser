package com.joutvhu.fixedwidth.parser.handler;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FixedWidthReader<T> extends FixedWidthHandler implements StringReader<T> {
    public FixedWidthReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }
}
