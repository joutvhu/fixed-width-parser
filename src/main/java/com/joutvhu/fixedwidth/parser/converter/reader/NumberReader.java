package com.joutvhu.fixedwidth.parser.converter.reader;

import com.joutvhu.fixedwidth.parser.converter.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.validation.FixedFormat;

public class NumberReader extends FixedWidthReader<Object> {
    public NumberReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        Class<?> type = info.getType();
        if (!TypeConstants.INTEGER_NUMBER_TYPES.contains(type) && !TypeConstants.DECIMAL_NUMBER_TYPES.contains(type))
            this.skip();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = assembler.get(info);
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        String format = fixedFormat != null ? fixedFormat.format() : null;
        Object result = null;

        if (CommonUtil.isNotBlank(value))
            result = ObjectUtil.parseNumber(value, type, format);
        if (result == null && TypeConstants.NOT_NULL_TYPES.contains(type))
            throw new NumberFormatException("null");

        return result;
    }
}
