package com.joutvhu.fixedwidth.parser.support;

import com.google.re2j.Pattern;
import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.exception.FindTypeException;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Find final type
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface FinalTypeFinder {
    /**
     * Get all fixed fields of a object type
     *
     * @param type class
     * @return all fixed width fields
     */
    default List<Field> getFixedFields(Class<?> type) {
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
     * Get final type of a string by supper FixedObject
     *
     * @param assembler is {@link StringAssembler}
     * @param type      of supper {@link FixedObject}
     * @return final class
     */
    default Class<?> detectFinalType(StringAssembler assembler, Class<?> type) {
        FixedObject fixedObject = type.getAnnotation(FixedObject.class);
        if (fixedObject != null && CommonUtil.isNotBlank(fixedObject.subTypes())) {
            for (FixedObject.Type subType : fixedObject.subTypes()) {
                checkAssignableFrom(type, subType.value());
                if (checkSelectionCondition(subType, valueOf(assembler, type, subType)))
                    return detectFinalType(assembler, subType.value());
            }
            if (!void.class.equals(fixedObject.defaultSubType())) {
                checkAssignableFrom(type, fixedObject.defaultSubType());
                return detectFinalType(assembler, fixedObject.defaultSubType());
            }
        }
        return type;
    }

    /**
     * Check the subClass is extends from superClass
     *
     * @param superClass is super-class
     * @param subClass   is sub-class
     */
    default void checkAssignableFrom(Class<?> superClass, Class<?> subClass) {
        if (!superClass.isAssignableFrom(subClass)) {
            String message = String.format("%s class is not a subclass of %s class.",
                    subClass.getName(), superClass.getName());
            throw new FindTypeException(message, superClass);
        }
    }

    /**
     * Check that the final class is neither the interface nor the abstract class
     *
     * @param type is final class
     * @return final class
     */
    default Class<?> checkFinalClass(Class<?> type) {
        int modifiers = type.getModifiers();
        if (!Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers))
            return type;
        else {
            String message = String.format("Can't find the final class of %s.", type.getName());
            if (Modifier.isInterface(modifiers))
                message += String.format(" The %s is a interface.", type.getName());
            if (Modifier.isAbstract(modifiers))
                message += String.format(" The %s is a abstract class.", type.getName());
            throw new FindTypeException(message, type);
        }
    }

    /**
     * Check selection condition of class
     *
     * @param subType {@link FixedObject.Type}
     * @param value   for checking
     * @return can use this class?
     */
    default boolean checkSelectionCondition(FixedObject.Type subType, String value) {
        if (CommonUtil.isNotBlank(subType.oneOf()) && CommonUtil.listOf(subType.oneOf()).contains(value))
            return true;
        if (CommonUtil.isNotBlank(subType.matchWith()) && Pattern.matches(subType.matchWith(), value))
            return true;
        return false;
    }

    /**
     * Get value of field by {@link FixedObject.Type} annotation
     *
     * @param assembler {@link StringAssembler}
     * @param type      is class type
     * @param subType   {@link FixedObject.Type}
     * @return string value
     */
    default String valueOf(StringAssembler assembler, Class<?> type, FixedObject.Type subType) {
        if (subType.length() > 0)
            return assembler.get(subType.start(), subType.length());
        else if (CommonUtil.isNotBlank(subType.prop()))
            return valueOf(assembler, type, subType.prop());
        else return null;
    }

    /**
     * Get value of field by field name
     *
     * @param assembler {@link StringAssembler}
     * @param type      is class type
     * @param prop      is field name
     * @return string value
     */
    default String valueOf(StringAssembler assembler, Class<?> type, String prop) {
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
