package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

public class DateWriter extends FixedWidthWriter<Object> {
    public DateWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.DATE_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public String write(Object value) {
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);
        return ObjectUtil.formatDate(value, format);
    }
}
