package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class NumberWriter extends FixedWidthWriter<Object> {
    public NumberWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        Class<?> type = info.getType();
        if (!TypeConstants.INTEGER_NUMBER_TYPES.contains(type) && !TypeConstants.DECIMAL_NUMBER_TYPES.contains(type))
            this.reject();
    }

    @Override
    public String write(Object value) {
        Class<?> type = info.getType();
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);

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
