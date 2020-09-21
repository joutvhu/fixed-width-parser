package com.joutvhu.fixedwidth.parser.convert.general;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

/**
 * Check field is number field
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface NumberHelper {
    default boolean isNumeric(FixedTypeInfo info, FixedParseStrategy strategy) {
        Class<?> type = info.getType();
        boolean isDecimal = TypeConstants.DECIMAL_NUMBER_TYPES.contains(type);
        if (!TypeConstants.INTEGER_NUMBER_TYPES.contains(type) && !isDecimal)
            return false;
        this.setIsDecimal(isDecimal);
        return true;
    }

    void setIsDecimal(boolean isDecimal);
}
