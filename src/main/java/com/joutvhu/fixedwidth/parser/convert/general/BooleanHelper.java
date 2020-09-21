package com.joutvhu.fixedwidth.parser.convert.general;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

/**
 * Split boolean from options
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface BooleanHelper {
    String SPLIT_SYMBOL = "|";

    default boolean splitOptions(String format) {
        String split = CommonUtil.escapeRegular(SPLIT_SYMBOL);
        String regex = "^[^" + split + "]+" + split + "[^" + split + "]+$";
        if (CommonUtil.isNotBlank(format) && Pattern.matches(regex, format)) {
            String[] options = format.split(split);
            if (!options[0].equals(options[1])) {
                setOptions(options);
                return true;
            }
        }
        return false;
    }

    void setOptions(String[] options);
}
