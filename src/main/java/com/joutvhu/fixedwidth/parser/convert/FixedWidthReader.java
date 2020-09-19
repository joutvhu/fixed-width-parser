package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FixedWidthReader<T> extends ParsingApprover implements StringReader<T> {
    public FixedWidthReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }
}
