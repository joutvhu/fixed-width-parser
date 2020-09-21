package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Numeric validator
 *
 * @author Giao Ho
 * @since 1.0.1
 */
public class NumberValidator extends FixedWidthValidator implements NumberHelper {
    private boolean isDecimal;

    public NumberValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!isNumeric(info, strategy))
            this.reject();
    }

    @Override
    public void validate(String value, ValidationType type) {
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);

        if (fixedFormat != null && CommonUtil.isNotBlank(fixedFormat.format())) {
            DecimalFormat decimalFormat = new DecimalFormat(fixedFormat.format());
            decimalFormat.setParseBigDecimal(true);
            try {
                decimalFormat.parse(value);
            } catch (ParseException e) {
                String message = getMessage(fixedFormat.message(), fixedFormat.nativeMessage(),
                        "{title} with value \"{0}\" doesn't match the {1} format.",
                        value, fixedFormat.format());
                throw new InvalidException(message);
            }
        } else if (!NumberUtils.isCreatable(value)) {
            throw new InvalidException(info.buildMessage("{title} with value \"{0}\" is not a {1}.",
                    value, isDecimal ? "number" : "integer number"));
        }
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
        this.isDecimal = isDecimal;
    }
}
