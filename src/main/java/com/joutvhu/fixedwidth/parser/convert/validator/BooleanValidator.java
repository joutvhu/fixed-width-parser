package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.convert.ValidationType;
import com.joutvhu.fixedwidth.parser.convert.general.BooleanHelper;
import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BooleanValidator extends FormatValidator implements BooleanHelper {
    private List<String> options = new ArrayList<>();

    public BooleanValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.reject();
    }

    @Override
    public void setOptions(String[] options) {
        this.options = CommonUtil.listOf(options);
    }

    @Override
    public boolean validate(String value, ValidationType type) {
        if (splitOptions(fixedFormat.format()) && !options.contains(value)) {
            String message = getMessage(fixedFormat.message(),
                    fixedFormat.nativeMessage(),
                    "{title} should be equal to one of the following value(s): {0}.",
                    StringUtils.join(options, ", "));
            throw new InvalidException(message);
        }
        return true;
    }
}