package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

public class DateValidator extends FormatValidator {
    public DateValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.DATE_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public boolean validate(String value, ValidationType type) {
        if (CommonUtil.isNotBlank(fixedFormat.format()) &&
                !CommonUtil.isDateValid(value, fixedFormat.format(), true)) {
            String message = getMessage(fixedFormat.message(),
                    fixedFormat.nativeMessage(),
                    "{title} should be in format {0}.",
                    fixedFormat.format());
            throw new InvalidException(message);
        }
        return true;
    }
}
