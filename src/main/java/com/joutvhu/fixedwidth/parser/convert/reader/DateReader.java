package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class DateReader extends FixedWidthReader<Object> {
    public DateReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.DATE_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = assembler.getValue();
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);

        if (CommonUtil.isNotBlank(value))
            return ObjectUtil.parseDate(value, type, format);
        return null;
    }
}
