package com.joutvhu.fixedwidth.parser.writer.impl;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;

public class DateWriter extends FixedWidthWriter<Object> {
    public DateWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.DATE_TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public String write(Object value) {
        return null;
    }
}
