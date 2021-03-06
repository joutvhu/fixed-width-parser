package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class StringWriter extends FixedWidthWriter<Object> {
    public StringWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.STRING_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public String write(Object value) {
        String result = StringUtils.EMPTY;
        if (value != null) result += value;
        return result;
    }
}
