package com.joutvhu.fixedwidth.parser.convert.validator;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Numeric validator
 *
 * @author Giao Ho
 * @since 1.0.3
 */
public class NumberValidator extends FixedWidthValidator implements NumberHelper {
    private boolean isDecimal;

    public NumberValidator(FixedTypeInfo info) {
        super(info);
        if (!isNumeric(info))
            this.reject();
    }

    @Override
    public void validate(String value, ValidationType type) {
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        DecimalFormat decimalFormat = getDecimalFormat(fixedFormat);

        if (decimalFormat != null) {
            try {
                decimalFormat.parse(value);
            } catch (ParseException e) {
                String message = formatMessage(fixedFormat.message(), fixedFormat.nativeMessage(),
                        "{title} with value \"{value}\" does not match the {format} format.",
                        CommonUtil.putToMap(super.getArguments(value),
                                "{format}", () -> fixedFormat.format()));
                throw new InvalidException(message);
            }
        } else {
            String regex = isDecimal ? "^[0-9]+(\\.[0-9]+)?$" : "^[0-9]+$";
            if (!Pattern.matches(regex, value)) {
                throw new InvalidException(info.formatMessage(
                        "{title} with value \"{value}\" is not a {number_type}.",
                        CommonUtil.putToMap(super.getArguments(value),
                                "{number_type}", () -> isDecimal ? "number" : "integer")));
            }
        }
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
        this.isDecimal = isDecimal;
    }
}
