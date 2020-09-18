package com.joutvhu.fixedwidth.parser.handler.validator;

import com.joutvhu.fixedwidth.parser.handler.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.handler.ValidationType;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public class OptionValidator extends FixedWidthValidator {
    public OptionValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }

    @Override
    public boolean validate(String value, ValidationType type) {
        return false;
    }
}
