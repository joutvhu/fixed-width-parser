package com.joutvhu.fixedwidth.parser.handle.reader;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.handle.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

public class BooleanReader extends FixedWidthReader<Boolean> {
    public BooleanReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public Boolean read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = assembler.get(info);
        String format = info.getAnnotationValue(FixedFormat.class,"format", String.class);

        if (CommonUtil.isNotBlank(value)) {
            String trimValue = value.trim();
            if (format != null && Pattern.matches("^[^_]+_[^_]+$", format) &&
                    !Pattern.matches("^([^_]+)_\\1$", format)) {
                String[] options = format.split("_");
                if (options[0].equalsIgnoreCase(value) || options[0].equalsIgnoreCase(trimValue))
                    return true;
                if (options[1].equalsIgnoreCase(value) || options[1].equalsIgnoreCase(trimValue))
                    return false;
            }

            for (String v : new String[]{"Y", "T", "YES", "TRUE", "ON", "1"})
                if (v.equalsIgnoreCase(value) || v.equalsIgnoreCase(trimValue))
                    return true;
            for (String v : new String[]{"N", "F", "NO", "FALSE", "OFF", "0"})
                if (v.equalsIgnoreCase(value) || v.equalsIgnoreCase(trimValue))
                    return false;
            if (boolean.class.equals(type))
                return CommonUtil.isNotBlank(trimValue);
        }
        return boolean.class.equals(type) ? false : null;
    }
}
