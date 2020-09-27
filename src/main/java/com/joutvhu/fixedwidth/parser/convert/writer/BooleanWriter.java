package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.general.BooleanHelper;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class BooleanWriter extends FixedWidthWriter<Boolean> implements BooleanHelper {
    private String[] options;

    public BooleanWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public void setOptions(String[] options) {
        this.options = options;
    }

    @Override
    public String write(Boolean value) {
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);

        if (value != null) {
            if (!splitOptions(format)) {
                if (info.getLength() > 4)
                    options = new String[]{"TRUE", "FALSE"};
                else if (info.getLength() > 2)
                    options = new String[]{"YES", "NO"};
                else options = new String[]{"T", "F"};
            }
            if (Boolean.TRUE.equals(value))
                return options[0];
            else if (Boolean.FALSE.equals(value))
                return options[1];
        }
        return StringUtils.EMPTY;
    }
}
