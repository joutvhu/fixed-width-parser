package com.joutvhu.fixedwidth.parser.handle.validator;

import com.joutvhu.fixedwidth.parser.exception.InvalidException;
import com.joutvhu.fixedwidth.parser.handle.ValidationType;
import com.joutvhu.fixedwidth.parser.handle.general.BooleanHandler;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BooleanValidator extends FormatValidator implements BooleanHandler {
    private List<String> options = new ArrayList<>();

    public BooleanValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType()))
            this.skip();
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
                    "{title} should be in format {0}.",
                    StringUtils.join(options, "/"));
            throw new InvalidException(message);
        }
        return true;
    }
}
