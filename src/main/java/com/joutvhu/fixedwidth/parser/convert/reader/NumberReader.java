package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.exception.ParserException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class NumberReader extends FixedWidthReader<Object> implements NumberHelper {
    public NumberReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!isNumeric(info, strategy))
            this.reject();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = assembler.getValue();
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);
        Object result = null;

        if (CommonUtil.isNotBlank(value)) {
            try {
                result = ObjectUtil.parseNumber(value, type, format);
            } catch (Exception e) {
                throw new ParserException(info.buildMessage("{title} is not a number."));
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
