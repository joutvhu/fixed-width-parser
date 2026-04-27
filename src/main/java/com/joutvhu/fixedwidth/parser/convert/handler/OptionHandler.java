package com.joutvhu.fixedwidth.parser.convert.handler;

import com.joutvhu.fixedwidth.parser.constraint.FixedOption;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates that a field value is (or is not) one of the allowed options.
 * Runs at {@link Phase#READ_AFTER_TRANSFORM} and {@link Phase#WRITE_AFTER_TRANSFORM}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class OptionHandler implements Hook {

    @Override
    public Set<Phase> getSupportedPhases() {
        return new HashSet<>(Arrays.asList(
            Phase.READ_AFTER_TRANSFORM,
            Phase.WRITE_AFTER_TRANSFORM));
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        FixedOption annotation = info.getAnnotation(FixedOption.class);
        if (annotation == null) return;

        String value = ctx.getPhase().isRead()
            ? ctx.getProcessedString()
            : (String) ctx.getCurrentValue();

        if (CommonUtil.isBlank(value)) return;

        // Normalise value and options according to keepPadding setting
        boolean keepPadding = info.getDefaultKeepPadding();
        String normValue = keepPadding ? value
            : FixedStringAssembler.of(value).trim(info).getValue();

        List<String> options = new ArrayList<>();
        for (String opt : annotation.options()) {
            options.add(keepPadding ? opt
                : FixedStringAssembler.of(opt).trim(info).getValue());
        }

        boolean inList = options.contains(normValue);
        if (annotation.contains() != inList) {
            String message = buildMessage(annotation, info, value, options);
            throw new FixedValidationException(message);
        }
    }

    private String buildMessage(FixedOption annotation, FixedTypeInfo info,
                                String value, List<String> options) {
        if (CommonUtil.isNotBlank(annotation.message())) {
            return annotation.nativeMessage()
                ? annotation.message()
                : info.formatMessage(annotation.message(),
                CommonUtil.mapOfEntries(
                    CommonUtil.mapEntryOf("{value}", () -> value),
                    CommonUtil.mapEntryOf("{options}", () -> "\"" + StringUtils.join(options, "\", \"") + "\"")));
        }
        String template = annotation.contains()
            ? "{label} at position {position} should be equal to one of the following value(s): {options}."
            : "{label} at position {position} cannot be one of the following value(s): {options}.";
        return info.formatMessage(template,
            CommonUtil.mapOfEntries(
                CommonUtil.mapEntryOf("{value}", () -> value),
                CommonUtil.mapEntryOf("{options}", () -> "\"" + StringUtils.join(options, "\", \"") + "\"")));
    }
}
