package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;

/**
 * Fixed width reader
 *
 * @param <T> result type
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class FixedWidthReader<T> extends ParsingApprover<ReadStrategy>
        implements StringReader<T>, ReadStrategy {
    public FixedWidthReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
    }

    @Override
    public Object read(FixedTypeInfo info, StringAssembler assembler) {
        return strategy.read(info, assembler);
    }
}
