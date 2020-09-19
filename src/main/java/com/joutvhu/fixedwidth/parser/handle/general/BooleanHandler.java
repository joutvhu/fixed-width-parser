package com.joutvhu.fixedwidth.parser.handle.general;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

public interface BooleanHandler {
    String SPLIT_SYMBOL = "_";

    default boolean splitOptions(String format) {
        String regex = "^[^" + SPLIT_SYMBOL + "]+" + SPLIT_SYMBOL + "[^" + SPLIT_SYMBOL + "]+$";
        if (CommonUtil.isNotBlank(format) && Pattern.matches(regex, format)) {
            String[] options = format.split(SPLIT_SYMBOL);
            if (!options[0].equals(options[1])) {
                setOptions(options);
                return true;
            }
        }
        return false;
    }

    void setOptions(String[] options);
}
