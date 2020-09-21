package com.joutvhu.fixedwidth.parser.convert.validator;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

/**
 * Validator for {@link FixedRegex} annotation
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class RegexValidator extends FixedWidthValidator {
    private FixedRegex fixedRegex;

    public RegexValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        this.fixedRegex = info.getAnnotation(FixedRegex.class);
        if (fixedRegex == null) this.reject();
    }

    @Override
    public void validate(String value, ValidationType type) {
        if (CommonUtil.isNotBlank(fixedRegex.regex()) && !Pattern.matches(fixedRegex.regex(), value)) {
            String message = getMessage(fixedRegex.message(),
                    fixedRegex.nativeMessage(),
                    "{title} doesn't match the /{0}/ regex.",
                    fixedRegex.regex());
            throw new InvalidException(message);
        }
    }
}
