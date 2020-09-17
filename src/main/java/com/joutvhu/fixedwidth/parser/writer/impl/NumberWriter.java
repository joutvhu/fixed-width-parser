package com.joutvhu.fixedwidth.parser.writer.impl;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.validator.FixedFormat;
import com.joutvhu.fixedwidth.parser.writer.FixedWidthWriter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class NumberWriter extends FixedWidthWriter<Object> {
    public NumberWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        Class<?> type = info.getType();
        if (!TypeConstants.INTEGER_NUMBER_TYPES.contains(type) && !TypeConstants.DECIMAL_NUMBER_TYPES.contains(type))
            this.skip();
    }

    @Override
    public String write(Object value) {
        Class<?> type = info.getType();
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        String format = fixedFormat != null ? fixedFormat.format() : null;

        if (CommonUtil.isBlank(format))
            return CommonUtil.listOf(BigDecimal.class, BigInteger.class).contains(type) ?
                    value.toString() : value + StringUtils.EMPTY;
        else {
            DecimalFormat decimalFormat = new DecimalFormat(format);
            decimalFormat.setParseBigDecimal(true);
            return decimalFormat.format(value);
        }
    }
}
