package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Validator for {@link FixedOption} annotation
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class OptionValidator extends FixedWidthValidator {
    private FixedOption fixedOption;

    public OptionValidator(FixedTypeInfo info) {
        super(info);
        this.fixedOption = info.getAnnotation(FixedOption.class);
        if (fixedOption == null) this.reject();
    }

    @Override
    public void validate(String value, ValidationType type) {
        if (CommonUtil.isNotBlank(fixedOption.options())) {
            List<String> options = CommonUtil.listOf(fixedOption.options());
            if (fixedOption.contains() != options.contains(value)) {
                String message = fixedOption.contains() ?
                        "{label} at position {position} should be equal to one of the following value(s): {options}." :
                        "{label} at position {position} cannot be one of the following value(s): {options}.";
                message = formatMessage(fixedOption.message(), fixedOption.nativeMessage(),
                        message, CommonUtil.putToMap(super.getArguments(value),
                                "{options}", () -> "\"" + StringUtils.join(options, "\", \"") + "\""));
                throw new InvalidException(message);
            }
        }
    }
}
