package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class NumberWriter extends FixedWidthWriter<Object> implements NumberHelper {
    public NumberWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!isNumeric(info, strategy))
            this.reject();
    }

    @Override
    public String write(Object value) {
        Class<?> type = info.getType();
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        DecimalFormat decimalFormat = getDecimalFormat(fixedFormat);

        if (decimalFormat == null)
            return CommonUtil.listOf(BigDecimal.class, BigInteger.class).contains(type) ?
                    value.toString() : value + StringUtils.EMPTY;
        else {
            return decimalFormat.format(value);
        }
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
    }
}
