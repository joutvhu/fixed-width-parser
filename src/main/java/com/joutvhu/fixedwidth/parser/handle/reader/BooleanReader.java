package com.joutvhu.fixedwidth.parser.handle.reader;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.handle.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.handle.general.BooleanHandler;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

public class BooleanReader extends FixedWidthReader<Boolean> implements BooleanHandler {
    private String trueOption;
    private String falseOption;

    public BooleanReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public void setOptions(String[] options) {
        this.trueOption = options[0];
        this.falseOption = options[1];
    }

    @Override
    public Boolean read(StringAssembler assembler) {
        Class<?> type = info.getType();
        String value = assembler.get(info);
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);

        if (CommonUtil.isNotBlank(value)) {
            String trimValue = value.trim();
            if (splitOptions(format)) {
                if (trueOption.equalsIgnoreCase(value) || trueOption.equalsIgnoreCase(trimValue))
                    return true;
                if (falseOption.equalsIgnoreCase(value) || falseOption.equalsIgnoreCase(trimValue))
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
