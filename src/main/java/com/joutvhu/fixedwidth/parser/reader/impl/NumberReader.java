package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.validator.FixedFormat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class NumberReader extends FixedWidthReader<Object> {
    private static DecimalFormatSymbols symbols;

    private boolean notNull;
    private boolean isDecimal;

    public NumberReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        Class<?> type = info.getType();
        this.isDecimal = TypeConstants.DECIMAL_NUMBER_TYPES.contains(type);
        if (!TypeConstants.INTEGER_NUMBER_TYPES.contains(type) && !this.isDecimal)
            this.skip();
        else {
            this.notNull = CommonUtil
                    .listOf(short.class, int.class, long.class, float.class, double.class)
                    .contains(type);
        }
    }

    @Override
    public Object read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = assembler.get(info);
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        String format = fixedFormat != null ? fixedFormat.format() : null;
        Object result = null;


        if (CommonUtil.isNotBlank(value)) {
            if (isDecimal && CommonUtil.isNotBlank(format))
                result = parseDecimalWithFormat(value, format);
            else if (short.class.equals(type) || Short.class.equals(type))
                result = Short.valueOf(value);
            else if (int.class.equals(type) || Integer.class.equals(type))
                result = Integer.valueOf(value);
            else if (long.class.equals(type) || Long.class.equals(type))
                result = Long.valueOf(value);
            else if (float.class.equals(type) || Float.class.equals(type))
                result = Float.valueOf(value);
            else if (double.class.equals(type) || Double.class.equals(type))
                result = Double.valueOf(value);
            else if (BigInteger.class.equals(type))
                result = new BigInteger(value);
            else if (BigDecimal.class.equals(type))
                result = new BigDecimal(value);
            else result = ObjectUtil.readValue(value, type);
        }

        if (result == null && notNull)
            throw new NumberFormatException("null");

        return result;
    }

    private Object parseDecimalWithFormat(String value, String format) {
        Class<?> type = info.getType();

        try {
            DecimalFormat decimalFormat = new DecimalFormat(format);
            decimalFormat.setDecimalFormatSymbols(getDecimalFormatSymbols());
            decimalFormat.setParseBigDecimal(true);

            BigDecimal number = (BigDecimal) decimalFormat.parse(value);

            if (float.class.equals(type) || Float.class.equals(type))
                return number.floatValue();
            else if (double.class.equals(type) || Double.class.equals(type))
                return number.doubleValue();
            else if (BigDecimal.class.equals(type))
                return number;
        } catch (ParseException e) {
            return ObjectUtil.readValue(value, type);
        }
        return null;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        if (symbols == null) {
            symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');
        }
        return symbols;
    }
}
