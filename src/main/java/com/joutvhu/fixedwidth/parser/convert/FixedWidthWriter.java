package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

/**
 * Fixed width writer
 *
 * @param <T> input type
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class FixedWidthWriter<T> extends ParsingApprover implements StringWriter<T> {
    public FixedWidthWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }
}
