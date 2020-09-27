package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;

import java.util.Collection;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class CollectionWriter extends FixedWidthWriter<Collection<?>> {
    protected FixedTypeInfo valueInfo;
    protected Integer start = 0;
    protected Integer length = 0;

    public CollectionWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
        if (!Collection.class.isAssignableFrom(info.getType()) || info.getGenericTypeInfo().size() != 1)
            this.reject();
        this.valueInfo = info.getGenericTypeInfo().get(0);
        this.length = valueInfo.getLength();
    }

    @Override
    public String write(Collection<?> value) {
        StringAssembler assembler = FixedStringAssembler.instance();
        for (Object item : value) {
            assembler.set(start, length, write(valueInfo, item));
            start += length;
        }
        return assembler.getValue();
    }
}
