package com.joutvhu.fixedwidth.parser.convert.handler;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.exception.FixedValidationException;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Set;

/**
 * Validates numeric fields, optionally against a {@link FixedFormat} pattern.
 * Runs at {@link Phase#READ_AFTER_TRANSFORM}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class NumberHandler implements Hook, NumberHelper {

    private boolean isDecimal;

    @Override
    public Set<Phase> getSupportedPhases() {
        return Collections.singleton(Phase.READ_AFTER_TRANSFORM);
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        FixedFormat annotation = info.getAnnotation(FixedFormat.class);
        if (annotation == null) return;

        // Only applies to numeric types
        if (!isNumeric(info)) return;

        String value = ctx.getProcessedString();
        if (CommonUtil.isBlank(value)) return;

        DecimalFormat decimalFormat = getDecimalFormat(annotation);
        if (decimalFormat != null) {
            try {
                decimalFormat.parse(value);
            } catch (ParseException e) {
                String message = info.formatMessage(
                    "{title} with value \"{value}\" does not match the {format} format.",
                    CommonUtil.mapOfEntries(
                        CommonUtil.mapEntryOf("{value}", () -> value),
                        CommonUtil.mapEntryOf("{format}", annotation::format)));
                throw new FixedValidationException(message);
            }
        } else {
            String regex = isDecimal ? "^ *[0-9]*(\\.[0-9]+)? *$" : "^ *[0-9]+ *$";
            if (!com.google.re2j.Pattern.matches(regex, value) || ".".equals(value.trim())) {
                String numType = isDecimal ? "number" : "integer";
                String article = isDecimal ? "a" : "an";
                String message = info.formatMessage(
                    "{title} with value \"{value}\" is not " + article + " {number_type}.",
                    CommonUtil.mapOfEntries(
                        CommonUtil.mapEntryOf("{value}", () -> value),
                        CommonUtil.mapEntryOf("{number_type}", () -> numType)));
                throw new FixedValidationException(message);
            }
        }
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
        this.isDecimal = isDecimal;
    }
}
