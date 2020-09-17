package com.joutvhu.fixedwidth.parser.writer.impl;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;
import org.apache.commons.lang3.StringUtils;

public class StringWriter extends FixedWidthWriter<Object> {
    public StringWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.STRING_TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public String write(Object value) {
        String result = StringUtils.EMPTY;
        if (value != null) result += value;
        return result;
    }
}
