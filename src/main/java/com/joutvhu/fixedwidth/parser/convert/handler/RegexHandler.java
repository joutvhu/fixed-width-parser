package com.joutvhu.fixedwidth.parser.convert.handler;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.constraint.FixedRegex;
import com.joutvhu.fixedwidth.parser.convert.AnnotationHandler;
import com.joutvhu.fixedwidth.parser.exception.RegexMismatchException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates that a field value matches a regular expression.
 * Runs at {@link Phase#READ_AFTER_TRANSFORM} and {@link Phase#WRITE_AFTER_TRANSFORM}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class RegexHandler implements AnnotationHandler<FixedRegex> {

    @Override
    public Set<Phase> getPhases(FixedRegex annotation) {
        return new HashSet<>(Arrays.asList(
                Phase.READ_AFTER_TRANSFORM,
                Phase.WRITE_AFTER_TRANSFORM));
    }

    @Override
    public void handle(FixedRegex annotation, FixedTypeInfo info, ParseContext ctx) {
        String value = ctx.getPhase().isRead()
                ? ctx.getProcessedString()
                : (String) ctx.getCurrentValue();

        if (CommonUtil.isBlank(value)) return; // blank is handled elsewhere

        if (CommonUtil.isNotBlank(annotation.regex()) &&
                !Pattern.compile(annotation.regex(), annotation.flags()).matches(value)) {
            String message = buildMessage(annotation, info, value);
            throw new RegexMismatchException(message);
        }
    }

    private String buildMessage(FixedRegex annotation, FixedTypeInfo info, String value) {
        if (CommonUtil.isNotBlank(annotation.message())) {
            return annotation.nativeMessage()
                    ? annotation.message()
                    : info.formatMessage(annotation.message(),
                            CommonUtil.mapOfEntries(
                                    CommonUtil.mapEntryOf("{value}", () -> value),
                                    CommonUtil.mapEntryOf("{regex}", () -> "/" + annotation.regex() + "/")));
        }
        return info.formatMessage("{title} does not match the {regex} regex.",
                CommonUtil.mapOfEntries(
                        CommonUtil.mapEntryOf("{value}", () -> value),
                        CommonUtil.mapEntryOf("{regex}", () -> "/" + annotation.regex() + "/")));
    }
}
