package com.joutvhu.fixedwidth.parser.convert.hook;

import com.joutvhu.fixedwidth.parser.constraint.FixedEnum;
import com.joutvhu.fixedwidth.parser.convert.ModuleHook;
import com.joutvhu.fixedwidth.parser.support.FixedStringAssembler;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Set;

/**
 * Handles enum fields.
 *
 * <p>READ at {@link Phase#READ_AFTER_TRANSFORM}: parses string to enum constant.
 * WRITE at {@link Phase#WRITE_AFTER_GET}: converts enum constant to string.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public class EnumHook implements ModuleHook {

    @Override
    public boolean supports(FixedTypeInfo info) {
        return info.getType().isEnum();
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleRead(FixedTypeInfo info, ParseContext ctx) {
        Class<Enum> type = (Class<Enum>) info.getType();
        String processedString = ctx.getProcessedString();
        String value = processedString != null
            ? FixedStringAssembler.of(processedString).trim(info).getValue()
            : null;

        if (CommonUtil.isBlank(value)) {
            ctx.setCurrentValue(null);
            return;
        }

        FixedEnum fixedEnum = info.getAnnotation(FixedEnum.class);
        String property = fixedEnum != null ? fixedEnum.property() : "";
        boolean ignoreCase = fixedEnum == null || fixedEnum.ignoreCase();

        Enum<?>[] constants = type.getEnumConstants();
        if (constants != null) {
            for (Enum<?> constant : constants) {
                String compareValue = null;
                if (CommonUtil.isNotBlank(property)) {
                    try {
                        Field field = type.getDeclaredField(property);
                        ReflectionUtil.makeAccessible(field);
                        Object v = ReflectionUtil.getField(field, constant);
                        if (v != null) compareValue = v.toString();
                    } catch (Exception ignored) {
                    }
                } else {
                    compareValue = constant.name();
                }
                if (compareValue != null) {
                    if (ignoreCase ? compareValue.equalsIgnoreCase(value) : compareValue.equals(value)) {
                        ctx.setCurrentValue(constant);
                        return;
                    }
                }
            }
        }
        ctx.setCurrentValue(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleWrite(FixedTypeInfo info, ParseContext ctx) {
        Object rawValue = ctx.getCurrentValue();
        if (rawValue == null) {
            ctx.setCurrentValue(StringUtils.EMPTY);
            return;
        }
        Enum<?> value = (Enum<?>) rawValue;
        FixedEnum fixedEnum = info.getAnnotation(FixedEnum.class);
        String property = fixedEnum != null ? fixedEnum.property() : "";

        if (CommonUtil.isNotBlank(property)) {
            try {
                Field field = value.getDeclaringClass().getDeclaredField(property);
                ReflectionUtil.makeAccessible(field);
                Object v = ReflectionUtil.getField(field, value);
                ctx.setCurrentValue(v != null ? v.toString() : StringUtils.EMPTY);
                return;
            } catch (Exception ignored) {
            }
        }
        ctx.setCurrentValue(value.name());
    }
}
