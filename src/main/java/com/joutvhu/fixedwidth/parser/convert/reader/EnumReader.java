package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.constraint.FixedEnum;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.lang.reflect.Field;

/**
 * Reads a fixed-width string into an Enum constant.
 *
 * @author Giao Ho
 * @since 1.2.1
 */
public class EnumReader extends FixedWidthReader<Enum<?>> {
    public EnumReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (!info.getType().isEnum())
            this.reject();
    }

    @Override
    public Enum<?> read(StringAssembler assembler) {
        Class<Enum> type = (Class<Enum>) info.getType();
        String value = assembler.trim(info).getValue();
        if (CommonUtil.isBlank(value)) return null;

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
                    if (ignoreCase ? compareValue.equalsIgnoreCase(value) : compareValue.equals(value))
                        return constant;
                }
            }
        }

        return null;
    }
}
