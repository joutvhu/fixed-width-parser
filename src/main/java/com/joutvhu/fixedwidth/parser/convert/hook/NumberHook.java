package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.convert.general.NumberHelper;
import com.joutvhu.fixedwidth.parser.exception.TypeConversionException;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.Set;

/**
 * Handles integer and decimal number fields.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses string to number.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: converts number to string.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class NumberHook implements ModuleHook, NumberHelper {

    private boolean isDecimal;

    @Override
    public boolean supports(FixedTypeInfo info) {
        return isNumeric(info);
    }

    @Override
    public Set<Phase> getSupportedPhases() {
        return EnumSet.of(Phase.READ_AFTER_TRANSFORM, Phase.WRITE_AFTER_GET);
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        if (ctx.getPhase().isRead()) {
            handleRead(info, ctx);
        } else {
            handleWrite(info, ctx);
        }
    }

    private void handleRead(FixedTypeInfo info, ParseContext ctx) {
        Class<?> type = info.getType();
        String rawString = ctx.getRawString();
        String processedString = ctx.getProcessedString();
        String originalValue = rawString != null ? rawString : (processedString != null ? processedString : StringUtils.EMPTY);
        String value = FixedStringAssembler.of(originalValue).trim(info).getValue().trim();
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        Object result = null;

        if (CommonUtil.isNotBlank(value)) {
            try {
                result = ObjectUtil.parseNumber(value, type, getDecimalFormat(fixedFormat));
            } catch (Exception e) {
                throw new TypeConversionException(info.buildMessage("{title} is not a number."));
            }
        } else if (CommonUtil.isNotBlank(originalValue)) {
            try {
                result = ObjectUtil.parseNumber("0", type, getDecimalFormat(fixedFormat));
            } catch (Exception ignored) {
            }
        }
        if (result == null && info.isRequire())
            throw new NumberFormatException(info.buildMessage("{title} cannot be null."));

        ctx.setCurrentValue(result);
    }

    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object value = ctx.getCurrentValue();
        if (value == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }
        Class<?> type = info.getType();
        FixedFormat fixedFormat = info.getAnnotation(FixedFormat.class);
        DecimalFormat decimalFormat = getDecimalFormat(fixedFormat);

        String result;
        if (decimalFormat == null) {
            result = CommonUtil.listOf(BigDecimal.class, BigInteger.class).contains(type)
                ? value.toString() : value + StringUtils.EMPTY;
        } else {
            result = decimalFormat.format(value);
        }
        ctx.setCurrentValue(result);
    }

    @Override
    public void setIsDecimal(boolean isDecimal) {
        this.isDecimal = isDecimal;
    }
}
