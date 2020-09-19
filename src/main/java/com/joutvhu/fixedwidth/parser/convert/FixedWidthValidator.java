package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

public abstract class FixedWidthValidator extends ParsingApprover implements StringValidator {
    public FixedWidthValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }

    protected String getMessage(String message, boolean nativeMessage, String defaultMessage, Object... arguments) {
        if (CommonUtil.isBlank(message)) {
            message = defaultMessage;
            nativeMessage = false;
        }
        if (!nativeMessage)
            message = info.buildMessage(message, arguments);
        return message;
    }
}
