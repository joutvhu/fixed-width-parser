package com.joutvhu.fixedwidth.parser.handle.validator;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.handle.ValidationType;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

public class BooleanValidator extends FormatValidator {
    public BooleanValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.skip();
    }

    @Override
    public boolean validate(String value, ValidationType type) {
        if (CommonUtil.isNotBlank(fixedFormat.format()) &&
                Pattern.matches("^[^_]+_[^_]+$", fixedFormat.format()) &&
                !Pattern.matches("^([^_]+)_\\1$", fixedFormat.format())) {
            String[] options = fixedFormat.format().split("_");
            if (options.length == 2 && !CommonUtil.listOf(options).contains(value)) {
                String message = getMessage(fixedFormat.message(),
                        fixedFormat.nativeMessage(),
                        "{title} should be in format {}.",
                        fixedFormat.format());
                throw new InvalidException(message);
            }
        }
        return true;
    }
}
