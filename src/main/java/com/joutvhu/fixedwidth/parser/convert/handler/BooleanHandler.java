package com.joutvhu.fixedwidth.parser.convert.handler;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.convert.general.BooleanHelper;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Validates boolean fields annotated with {@link FixedFormat} using a "true|false" format.
 * Runs at {@link Phase#READ_AFTER_TRANSFORM}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class BooleanHandler implements Hook, BooleanHelper {

    private String[] options;

    @Override
    public Set<Phase> getSupportedPhases() {
        return Collections.singleton(Phase.READ_AFTER_TRANSFORM);
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        FixedFormat annotation = info.getAnnotation(FixedFormat.class);
        if (annotation == null) return;

        // Only applies to boolean types
        if (!TypeConstants.BOOLEAN_TYPES.contains(info.getType())) return;

        String value = ctx.getProcessedString();
        if (CommonUtil.isBlank(value)) return;

        if (splitOptions(annotation.format()) && !Arrays.asList(options).contains(value)) {
            String message = info.formatMessage(
                "{title} should be equal to one of the following value(s): {format}.",
                CommonUtil.mapOfEntries(
                    CommonUtil.mapEntryOf("{value}", () -> value),
                    CommonUtil.mapEntryOf("{format}", annotation::format)));
            throw new FixedValidationException(message);
        }
    }

    @Override
    public void setOptions(String[] options) {
        this.options = options;
    }
}
