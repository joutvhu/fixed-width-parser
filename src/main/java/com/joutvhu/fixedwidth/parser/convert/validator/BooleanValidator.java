package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.convert.general.BooleanHelper;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Boolean validator
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class BooleanValidator extends FormatValidator implements BooleanHelper {
    private List<String> options = new ArrayList<>();

    public BooleanValidator(FixedTypeInfo info) {
        super(info);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public void setOptions(String[] options) {
        this.options = CommonUtil.listOf(options);
    }

    @Override
    public void validate(String value, ValidationType type) {
        if (splitOptions(fixedFormat.format()) && !options.contains(value)) {
            String message = formatMessage(fixedFormat.message(),
                    fixedFormat.nativeMessage(),
                    "{title} should be equal to one of the following value(s): {format}.",
                    getArguments(value));
            throw new InvalidException(message);
        }
    }
}
