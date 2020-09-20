package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

/**
 * Fixed width validator
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class FixedWidthValidator extends ParsingApprover implements StringValidator {
    public FixedWidthValidator(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
    }

    /**
     * Get error message
     *
     * @param message        native message or message template
     * @param nativeMessage  the message is native message
     * @param defaultMessage use this message if the message is blank
     * @param arguments      the arguments
     * @return message
     */
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
