package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;

/**
 * Fixed width writer
 *
 * @param <T> input type
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class FixedWidthWriter<T> extends ParsingApprover<WriteStrategy>
        implements StringWriter<T>, WriteStrategy {
    public FixedWidthWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
    }

    @Override
    public String write(FixedTypeInfo info, Object value) {
        return strategy.write(info, value);
    }
}
