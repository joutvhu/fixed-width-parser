package com.joutvhu.fixedwidth.parser.convert.writer;

import com.joutvhu.fixedwidth.parser.constraint.FixedEnum;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * Writes an Enum constant into a fixed-width string.
 *
 * @author Giao Ho
 * @since 1.2.1
 */
public class EnumWriter extends FixedWidthWriter<Enum<?>> {
    public EnumWriter(FixedTypeInfo info, WriteStrategy strategy) {
        super(info, strategy);
        if (!info.getType().isEnum())
            this.reject();
    }

    @Override
    public String write(Enum<?> value) {
        if (value == null) return StringUtils.EMPTY;

        FixedEnum fixedEnum = info.getAnnotation(FixedEnum.class);
        String property = fixedEnum != null ? fixedEnum.property() : "";

        if (CommonUtil.isNotBlank(property)) {
            try {
                Field field = value.getDeclaringClass().getDeclaredField(property);
                ReflectionUtil.makeAccessible(field);
                Object v = ReflectionUtil.getField(field, value);
                return v != null ? v.toString() : StringUtils.EMPTY;
            } catch (Exception ignored) {
            }
        }

        return value.name();
    }
}
