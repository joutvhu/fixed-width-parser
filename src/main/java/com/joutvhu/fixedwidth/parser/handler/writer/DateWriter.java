package com.joutvhu.fixedwidth.parser.handler.writer;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthWriter;

public class DateWriter extends FixedWidthWriter<Object> {
    public DateWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.DATE_TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public String write(Object value) {
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        String format = fixedFormat != null ? fixedFormat.format() : null;
        return ObjectUtil.formatDate(value, format);
    }
}
