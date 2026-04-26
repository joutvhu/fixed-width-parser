package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
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
        if (com.joutvhu.fixedwidth.parser.util.CommonUtil.isNotBlank(value) && !info.getDefaultKeepPadding()) {
            value = com.joutvhu.fixedwidth.parser.support.FixedStringAssembler.of(value).trim(info).getValue();
        }
        if (CommonUtil.isNotBlank(fixedOption.options())) {
            List<String> options = new java.util.ArrayList<>();
            for (String opt : fixedOption.options()) {
                options.add(!info.getDefaultKeepPadding() ? com.joutvhu.fixedwidth.parser.support.FixedStringAssembler.of(opt).trim(info).getValue() : opt);
            }
            if (fixedOption.contains() != options.contains(value)) {
                String message = fixedOption.contains() ?
                        "{label} at position {position} should be equal to one of the following value(s): {options}." :
                        "{label} at position {position} cannot be one of the following value(s): {options}.";
                message = formatMessage(fixedOption.message(), fixedOption.nativeMessage(),
                        message, CommonUtil.putToMap(super.getArguments(value),
                                "{options}", () -> "\"" + StringUtils.join(options, "\", \"") + "\""));
                throw new FixedValidationException(message);
            }
        }
    }
}
