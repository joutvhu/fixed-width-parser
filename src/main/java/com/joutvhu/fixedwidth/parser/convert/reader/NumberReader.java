package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.exception.TypeConversionException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class NumberReader extends FixedWidthReader<Object> implements NumberHelper {
    public NumberReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (!isNumeric(info))
            this.reject();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String originalValue = assembler.getValue();
        String value = assembler.trim(info).getValue().trim();
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        Object result = null;

        if (CommonUtil.isNotBlank(value)) {
            try {
                result = ObjectUtil.parseNumber(value, type, getDecimalFormat(fixedFormat));
            } catch (Exception e) {
                throw new TypeConversionException(info.buildMessage("{title} is not a number."));
            }
        } else if (CommonUtil.isNotBlank(originalValue)) {
            try {
                result = ObjectUtil.parseNumber("0", type, getDecimalFormat(fixedFormat));
            } catch (Exception ignored) {
            }
        }
        if (result == null && info.isRequire())
            throw new NumberFormatException(info.buildMessage("{title} cannot be null."));

        return result;
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
    }
}
