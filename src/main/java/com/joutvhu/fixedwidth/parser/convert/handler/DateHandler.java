package com.joutvhu.fixedwidth.parser.convert.handler;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.exception.TypeConversionException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

import java.util.Collections;
import java.util.Set;

/**
 * Validates date/time fields annotated with {@link FixedFormat}.
 * Runs at {@link Phase#READ_AFTER_TRANSFORM}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class DateHandler implements Hook {

    @Override
    public Set<Phase> getSupportedPhases() {
        return Collections.singleton(Phase.READ_AFTER_TRANSFORM);
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        FixedFormat annotation = info.getAnnotation(FixedFormat.class);
        if (annotation == null) return;

        // Only applies to date types
        if (!TypeConstants.DATE_TYPES.contains(info.getType())) return;

        String value = ctx.getProcessedString();
        if (CommonUtil.isBlank(value)) return;

        if (CommonUtil.isNotBlank(annotation.format()) &&
            !CommonUtil.isDateValid(value, annotation.format(), true)) {
            String message = buildMessage(annotation, info, value);
            throw new TypeConversionException(message);
        }
    }

    private String buildMessage(FixedFormat annotation, FixedTypeInfo info, String value) {
        if (CommonUtil.isNotBlank(annotation.message())) {
            return annotation.nativeMessage()
                ? annotation.message()
                : info.formatMessage(annotation.message(),
                CommonUtil.mapOfEntries(
                    CommonUtil.mapEntryOf("{value}", () -> value),
                    CommonUtil.mapEntryOf("{format}", annotation::format)));
        }
        return info.formatMessage("{title} does not match the {format} format.",
            CommonUtil.mapOfEntries(
                CommonUtil.mapEntryOf("{value}", () -> value),
                CommonUtil.mapEntryOf("{format}", annotation::format)));
    }
}
