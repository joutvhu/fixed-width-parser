package com.joutvhu.fixedwidth.parser.support;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class FixedObjectHelper {
    /**
     * New instance by object type.
     *
     * @param type class
     * @param <T>  is type of the object
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public <T> T newInstanceOf(Class<T> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor = type.getConstructor();
        if (constructor != null) return (T) constructor.newInstance();
        else throw new UnsupportedOperationException(String
                .format("%s class don't have a no-arg constructor.", type.getName()));
    }

    /**
     * Get all fixed fields of a object type
     */
    public List<Field> getFixedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> superType = type.getSuperclass();
        if (superType != null) fields.addAll(getFixedFields(superType));
        for (Field field : type.getDeclaredFields()) {
            FixedField fixedField = field.getAnnotation(FixedField.class);
            if (fixedField != null) fields.add(field);
        }
        return fields;
    }

    /**
     * Get real type of a string by supper FixedObject
     *
     * @param type of supper FixedObject
     * @return
     */
    public Class<?> detectType(StringAssembler assembler, Class<?> type) {
        FixedObject fixedObject = type.getAnnotation(FixedObject.class);
        if (fixedObject != null && CommonUtil.isNotBlank(fixedObject.subTypes())) {
            for (FixedObject.Type subType : fixedObject.subTypes()) {
                if (type.isAssignableFrom(subType.value())) {
                    if (checkSubType(subType, valueOf(assembler, type, subType)))
                        return detectType(assembler, subType.value());
                } else throw new UnsupportedOperationException(
                        String.format("%s class is not a subclass of %s class.",
                                subType.value().getName(), type.getName()));
            }
            if (!void.class.equals(fixedObject.defaultSubType())) {
                if (type.isAssignableFrom(fixedObject.defaultSubType()))
                    return detectType(assembler, fixedObject.defaultSubType());
                else throw new UnsupportedOperationException(
                        String.format("%s class is not a subclass of %s class.",
                                fixedObject.defaultSubType().getName(), type.getName()));
            }
        }
        return type;
    }

    private boolean checkSubType(FixedObject.Type subType, String value) {
        if (CommonUtil.isNotBlank(subType.oneOf()) && CommonUtil.listOf(subType.oneOf()).contains(value))
            return true;
        if (CommonUtil.isNotBlank(subType.matchWith()) && Pattern.compile(subType.matchWith()).matches(value))
            return true;
        return false;
    }

    public String valueOf(StringAssembler assembler, Class<?> type, FixedObject.Type subType) {
        if (subType.length() > 0)
            return assembler.get(subType.start(), subType.length());
        else if (CommonUtil.isNotBlank(subType.prop()))
            return valueOf(assembler, type, subType.prop());
        else return null;
    }

    public String valueOf(StringAssembler assembler, Class<?> type, String prop) {
        for (Field field : getFixedFields(type)) {
            if (field.getName().equals(prop)) {
                FixedField fixedField = field.getAnnotation(FixedField.class);
                if (fixedField != null)
                    return assembler.get(fixedField.start(), fixedField.length());
                else throw new UnsupportedOperationException(String.format("%s field is not a fixed field.", prop));
            }
        }
        throw new NullPointerException(String.format("Can't found %s field.", prop));
    }
}
