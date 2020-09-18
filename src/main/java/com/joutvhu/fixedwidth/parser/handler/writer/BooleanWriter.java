package com.joutvhu.fixedwidth.parser.handler.writer;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthWriter;
import org.apache.commons.lang3.StringUtils;

public class BooleanWriter extends FixedWidthWriter<Boolean> {
    public BooleanWriter(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public String write(Boolean value) {
        String result = StringUtils.EMPTY;
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        String format = fixedFormat != null ? fixedFormat.format() : null;

        if (value != null) {
            String[] options;
            if (Pattern.matches("^[^_]+_[^_]+$", format) && !Pattern.matches("^([^_]+)_\\1$", format))
                options = format.split("_");
            else if (info.getLength() > 4)
                options = new String[]{"TRUE", "FALSE"};
            else if (info.getLength() > 2)
                options = new String[]{"YES", "NO"};
            else options = new String[]{"T", "F"};

            if (Boolean.TRUE.equals(value))
                result = options[0];
            else if (Boolean.FALSE.equals(value))
                result = options[1];
        }
        return result;
    }
}
