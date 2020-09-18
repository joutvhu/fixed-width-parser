package com.joutvhu.fixedwidth.parser.handler.validator;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.handler.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.handler.ValidationType;
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
        if (!Pattern.compile(fixedRegex.regex()).matches(value)) {
            String message = fixedRegex.message();
            boolean nativeMessage = fixedRegex.nativeMessage();
            if (CommonUtil.isBlank(message)) {
                message = "{title} doesn't match by /{}/ regex.";
                nativeMessage = false;
            }
            if (!nativeMessage) {
                message = info.buildMessage(message);
            }
            throw new InvalidException(message);
        }
        return true;
    }
}
