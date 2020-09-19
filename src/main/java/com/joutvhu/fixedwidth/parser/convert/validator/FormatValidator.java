package com.joutvhu.fixedwidth.parser.convert.validator;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FormatValidator extends FixedWidthValidator {
    protected FixedFormat fixedFormat;

    public FormatValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        this.fixedFormat = info.getAnnotation(FixedFormat.class);
        if (fixedFormat == null) this.reject();
    }
}
