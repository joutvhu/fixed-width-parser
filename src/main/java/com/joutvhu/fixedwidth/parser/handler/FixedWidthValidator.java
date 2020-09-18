package com.joutvhu.fixedwidth.parser.handler;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;

public abstract class FixedWidthValidator extends FixedWidthHandler implements StringValidator {
    public FixedWidthValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }
}
