package com.joutvhu.fixedwidth.parser.handle;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FixedWidthWriter<T> extends FixedWidthHandler implements StringWriter<T> {
    public FixedWidthWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }
}
