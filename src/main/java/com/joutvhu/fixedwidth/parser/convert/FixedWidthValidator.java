package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Fixed width validator
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public abstract class FixedWidthValidator extends ParsingApprover<Void> implements StringValidator {
    public FixedWidthValidator(FixedTypeInfo info) {
        super(info);
    }

    protected Map<String, Supplier<String>> getArguments(String value) {
        return CommonUtil.mapOfEntries(
                CommonUtil.mapEntryOf("{value}", () -> value)
        );
    }

    /**
     * Get error message
     *
     * @param message        native message or message template
     * @param nativeMessage  the message is native message
     * @param defaultMessage use this message if the message is blank
     * @param arguments      the arguments
     * @return message
     * @deprecated use {@link FixedWidthValidator#formatMessage(String, boolean, String, Map)}
     */
    @Deprecated
    protected String getMessage(String message, boolean nativeMessage, String defaultMessage, Object... arguments) {
        if (CommonUtil.isBlank(message)) {
            message = defaultMessage;
            nativeMessage = false;
        }
        if (!nativeMessage)
            message = info.buildMessage(message, arguments);
        return message;
    }

    /**
     * Format error message
     *
     * @param message        native message or message template
     * @param nativeMessage  the message is native message
     * @param defaultMessage use this message if the message is blank
     * @param arguments      map
     * @return message
     */
    protected String formatMessage(String message, boolean nativeMessage, String defaultMessage,
                                   Map<String, Supplier<String>> arguments) {
        if (CommonUtil.isBlank(message)) {
            message = defaultMessage;
            nativeMessage = false;
        }
        if (!nativeMessage)
            message = info.formatMessage(message, arguments);
        return message;
    }
}
