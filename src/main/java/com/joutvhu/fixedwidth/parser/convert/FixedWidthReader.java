package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

/**
 * Fixed width reader
 *
 * @param <T> result type
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class FixedWidthReader<T> extends ParsingApprover implements StringReader<T> {
    public FixedWidthReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }
}
