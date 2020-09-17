package com.joutvhu.fixedwidth.parser.writer.impl;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.validator.FixedFormat;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;
import org.apache.commons.lang3.StringUtils;

public class NumberWriter extends FixedWidthWriter<Object> {
    private boolean notNull;
    private boolean isDecimal;

    public NumberWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        Class<?> type = info.getType();
        this.isDecimal = TypeConstants.DECIMAL_NUMBER_TYPES.contains(type);
        if (!TypeConstants.INTEGER_NUMBER_TYPES.contains(type) && !this.isDecimal)
            this.skip();
        else this.notNull = TypeConstants.NOT_NULL_TYPES.contains(type);
    }

    @Override
    public String write(Object value) {
        String result = StringUtils.EMPTY;
        Class<?> type = info.getType();
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        String format = fixedFormat != null ? fixedFormat.format() : null;
        return result;
    }
}
