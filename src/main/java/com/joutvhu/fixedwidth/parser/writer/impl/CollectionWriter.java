package com.joutvhu.fixedwidth.parser.writer.impl;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;

import java.util.Collection;

public class CollectionWriter extends FixedWidthWriter<Collection<?>> {
    public CollectionWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!Collection.class.equals(info.getType()) || !Collection.class.isAssignableFrom(info.getType()) ||
                info.getGenericInfo().size() != 1)
            this.skip();
    }

    @Override
    public String write(Collection<?> value) {
        return null;
    }
}
