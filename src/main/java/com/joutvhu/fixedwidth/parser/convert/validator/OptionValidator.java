package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class OptionValidator extends FixedWidthValidator {
    private FixedOption fixedOption;

    public OptionValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        this.fixedOption = info.getAnnotation(FixedOption.class);
        if (fixedOption == null) this.reject();
    }

    @Override
    public boolean validate(String value, ValidationType type) {
        if (CommonUtil.isNotBlank(fixedOption.options())) {
            List<String> options = CommonUtil.listOf(fixedOption.options());
            if (!options.contains(value)) {
                String message = getMessage(fixedOption.message(),
                        fixedOption.nativeMessage(),
                        "{label} at position {position} should be equal to one of the following value(s): {0}.",
                        StringUtils.join(", ", options));
                throw new InvalidException(message);
            }
        }
        return true;
    }
}
