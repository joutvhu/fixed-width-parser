package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.constraint.FixedBoolean;
import com.joutvhu.fixedwidth.parser.constraint.FixedFormat;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.convert.general.BooleanHelper;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Set;

/**
 * Handles {@code Boolean} and {@code boolean} fields.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses string to boolean.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: converts boolean to string.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class BooleanHook implements ModuleHook, BooleanHelper {

    private String trueOption;
    private String falseOption;

    @Override
    public boolean supports(FixedTypeInfo info) {
        return TypeConstants.BOOLEAN_TYPES.contains(info.getType());
    }

    @Override
    public Set<Phase> phases() {
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
        String value = ctx.getProcessedString();
        if (value == null) value = StringUtils.EMPTY;
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);

        if (CommonUtil.isNotBlank(value)) {
            String trimValue = value.trim();
            if (splitOptions(format)) {
                if (trueOption.equalsIgnoreCase(value) || trueOption.equalsIgnoreCase(trimValue)) {
                    ctx.setCurrentValue(true);
                    return;
                }
                if (falseOption.equalsIgnoreCase(value) || falseOption.equalsIgnoreCase(trimValue)) {
                    ctx.setCurrentValue(false);
                    return;
                }
            }

            FixedBoolean fixedBoolean = info.getAnnotation(FixedBoolean.class);
            String[] trueValues = fixedBoolean != null ? fixedBoolean.trueValues()
                : new String[]{"Y", "T", "YES", "TRUE", "ON", "1"};
            String[] falseValues = fixedBoolean != null ? fixedBoolean.falseValues()
                : new String[]{"N", "F", "NO", "FALSE", "OFF", "0"};

            for (String v : trueValues) {
                if (v.equalsIgnoreCase(value) || v.equalsIgnoreCase(trimValue)) {
                    ctx.setCurrentValue(true);
                    return;
                }
            }
            for (String v : falseValues) {
                if (v.equalsIgnoreCase(value) || v.equalsIgnoreCase(trimValue)) {
                    ctx.setCurrentValue(false);
                    return;
                }
            }
            if (boolean.class.equals(type)) {
                ctx.setCurrentValue(CommonUtil.isNotBlank(trimValue));
                return;
            }
        }
        ctx.setCurrentValue(boolean.class.equals(type) ? false : null);
    }

    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object rawValue = ctx.getCurrentValue();
        Boolean value = rawValue instanceof Boolean ? (Boolean) rawValue : null;
        String format = info.getAnnotationValue(FixedFormat.class, "format", String.class);
        String[] options = null;

        if (value != null) {
            if (!splitOptions(format)) {
                FixedBoolean fixedBoolean = info.getAnnotation(FixedBoolean.class);
                if (fixedBoolean != null && fixedBoolean.trueValues().length > 0
                    && fixedBoolean.falseValues().length > 0) {
                    options = new String[]{fixedBoolean.trueValues()[0], fixedBoolean.falseValues()[0]};
                } else if (info.getLength() > 4) {
                    options = new String[]{"TRUE", "FALSE"};
                } else if (info.getLength() > 2) {
                    options = new String[]{"YES", "NO"};
                } else {
                    options = new String[]{"T", "F"};
                }
            } else {
                options = new String[]{trueOption, falseOption};
            }
            ctx.setCurrentValue(Boolean.TRUE.equals(value) ? options[0] : options[1]);
        } else {
            ctx.setCurrentValue(StringUtils.EMPTY);
        }
    }

    @Override
    public void setOptions(String[] options) {
        if (options != null && options.length >= 2) {
            this.trueOption = options[0];
            this.falseOption = options[1];
        }
    }
}
