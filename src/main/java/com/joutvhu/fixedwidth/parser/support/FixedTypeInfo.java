package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.domain.Padding;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedTypeInfo extends TypeInfoSetter {
    private String title;

    protected FixedTypeInfo(Class<?> type) {
        super(type);
    }

    protected FixedTypeInfo(AnnotatedType annotatedType) {
        super(annotatedType);
    }

    protected FixedTypeInfo(Field field) {
        super(field);
    }

    protected FixedTypeInfo(Object value) {
        super(value);
    }

    public static FixedTypeInfo of(Class<?> type) {
        return new FixedTypeInfo(type).postConstruct();
    }

    public static FixedTypeInfo of(AnnotatedType annotatedType) {
        return new FixedTypeInfo(annotatedType).postConstruct();
    }

    public static FixedTypeInfo of(Field field) {
        return new FixedTypeInfo(field).postConstruct();
    }

    public static FixedTypeInfo of(Object value) {
        return new FixedTypeInfo(value).postConstruct();
    }

    public Integer getPosition() {
        return start + 1;
    }

    public String getTitle() {
        if (this.title == null)
            this.title = buildMessage("{label} at position {position} and length {length}");
        return this.title;
    }

    /**
     * Get default arguments
     *
     * @return default argument map
     */
    private Map<String, Supplier<String>> getDefaultArguments() {
        return CommonUtil.mapOfEntries(
                CommonUtil.mapEntryOf("{label}", () -> label),
                CommonUtil.mapEntryOf("{start}", start::toString),
                CommonUtil.mapEntryOf("{position}", () -> getPosition().toString()),
                CommonUtil.mapEntryOf("{length}", length::toString),
                CommonUtil.mapEntryOf("{title}", this::getTitle)
        );
    }

    /**
     * Build message from message template and arguments
     *
     * @param template  message template
     * @param arguments the arguments
     * @return message
     */
    public String buildMessage(String template, Object... arguments) {
        template = formatMessage(template, null);
        return MessageFormat.format(template, arguments);
    }

    /**
     * Format message with a message template and argument map
     *
     * @param template  of message
     * @param arguments map
     * @return message
     */
    public String formatMessage(String template, Map<String, Supplier<String>> arguments) {
        Assert.hasLength(template, "The template message cannot be black.");
        if (arguments == null) arguments = getDefaultArguments();
        else arguments.putAll(getDefaultArguments());
        return CommonUtil.formatMessage(template, arguments);
    }

    /**
     * Get default padding character
     *
     * @return default padding character
     */
    public char getDefaultPadding() {
        if (padding == null || padding == Padding.AUTO) {
            if ((TypeConstants.INTEGER_NUMBER_TYPES.contains(type) ||
                    TypeConstants.DECIMAL_NUMBER_TYPES.contains(type)) &&
                    (alignment == null || Alignment.AUTO == alignment || Alignment.LEFT == alignment))
                return '0';
            return ' ';
        }
        return padding;
    }

    /**
     * Get default null padding
     *
     * @return default null padding
     */
    public Character getDefaultNullPadding() {
        if (nullPadding == null || nullPadding == Padding.AUTO) {
            if (TypeConstants.STRING_TYPES.contains(type))
                return null;
            return ' ';
        }
        return nullPadding;
    }

    /**
     * Get default keep padding
     *
     * @return default keep padding
     */
    public boolean getDefaultKeepPadding() {
        if (keepPadding == null || KeepPadding.AUTO.equals(keepPadding)) {
            return TypeConstants.INTEGER_NUMBER_TYPES.contains(type) ||
                    TypeConstants.DECIMAL_NUMBER_TYPES.contains(type) ||
                    TypeConstants.STRING_TYPES.contains(type) ||
                    TypeConstants.NOT_NULL_TYPES.contains(type);
        }
        return KeepPadding.KEEP.equals(keepPadding);
    }

    /**
     * Get default alignment
     *
     * @return default alignment
     */
    public Alignment getDefaultAlignment() {
        if (alignment == null || Alignment.AUTO.equals(alignment)) {
            if (TypeConstants.INTEGER_NUMBER_TYPES.contains(type) || TypeConstants.DECIMAL_NUMBER_TYPES.contains(type))
                return Alignment.LEFT;
            return Alignment.RIGHT;
        }
        return alignment;
    }

    /**
     * Get value of annotation class by property name
     * If the annotation is not found then return default value of that property
     *
     * @param annotationClass annotation class
     * @param name            property name
     * @param type            result type class
     * @param <T>             result type
     * @return annotation value
     */
    public <T> T getAnnotationValue(Class<? extends Annotation> annotationClass, String name, Class<T> type) {
        Annotation annotation = getAnnotation(annotationClass);
        if (annotation != null)
            return ReflectionUtil.getValueFromAnnotation(name, type, annotation);
        return ReflectionUtil.getDefaultValueOfAnnotation(name, type, annotationClass);
    }

    /**
     * Check this type is extends from a class
     *
     * @param cls is class type
     * @return is extends
     */
    public boolean isAssignableTo(Class<?> cls) {
        return cls != null && cls.isAssignableFrom(type);
    }

    /**
     * Check this type is one of the class types
     *
     * @param classes the class types
     * @return is contains
     */
    public boolean isOneOfTypes(List<Class<?>> classes) {
        return classes != null && classes.contains(type);
    }
}
