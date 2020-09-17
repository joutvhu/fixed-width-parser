package com.joutvhu.fixedwidth.parser.writer.impl;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;

public class ObjectWriter extends FixedWidthWriter<Object> {
    public ObjectWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (info.getFixedObject() == null) this.skip();
    }

    @Override
    public String write(Object value) {
        return null;
    }
}
