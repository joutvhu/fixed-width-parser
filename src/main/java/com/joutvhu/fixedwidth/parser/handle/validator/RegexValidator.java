package com.joutvhu.fixedwidth.parser.handle.validator;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.handle.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.handle.ValidationType;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

public class RegexValidator extends FixedWidthValidator {
    private FixedRegex fixedRegex;

    public RegexValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        this.fixedRegex = info.getAnnotation(FixedRegex.class);
        if (fixedRegex == null) this.skip();
    }

    @Override
    public boolean validate(String value, ValidationType type) {
        if (CommonUtil.isNotBlank(fixedRegex.regex()) && !Pattern.matches(fixedRegex.regex(), value)) {
            String message = getMessage(fixedRegex.message(),
                    fixedRegex.nativeMessage(),
                    "{title} doesn't match by /{0}/ regex.",
                    fixedRegex.regex());
            throw new InvalidException(message);
        }
        return true;
    }
}
