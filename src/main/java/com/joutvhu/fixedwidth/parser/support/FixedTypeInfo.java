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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Immutable metadata info for a fixed-width type.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedTypeInfo extends TypeInfoSetter {
    private final String title;

    protected FixedTypeInfo(Class<?> type) {
        super(type);
        this.title = buildMessage("{label} at position {position} and length {length}");
    }

    protected FixedTypeInfo(AnnotatedType annotatedType) {
        super(annotatedType);
        this.title = buildMessage("{label} at position {position} and length {length}");
    }

    protected FixedTypeInfo(Field field) {
        super(field);
        this.title = buildMessage("{label} at position {position} and length {length}");
    }

    protected FixedTypeInfo(Object value) {
        super(value.getClass());
        this.title = buildMessage("{label} at position {position} and length {length}");
    }

    protected FixedTypeInfo(FixedTypeInfo info, Class<?> type) {
        super(type, info.name, info.label, info.start, info.length, info.require,
                info.padding, info.nullPadding, info.keepPadding, info.alignment,
                info.field, info.annotatedType, info.fixedField, info.fixedParam,
                info.fixedObject, info.getSourceType(), true);
        this.title = buildMessage("{label} at position {position} and length {length}");
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

    public FixedTypeInfo detectTypeWith(StringAssembler assembler) {
        Class<?> detectedType = super.detectFinalClassWith(assembler);
        if (this.type.equals(detectedType)) return this;
        return new FixedTypeInfo(this, detectedType);
    }

    public FixedTypeInfo detectTypeWith(Object value) {
        Class<?> detectedType = super.detectFinalClassWith(value);
        if (this.type.equals(detectedType)) return this;
        return new FixedTypeInfo(this, detectedType);
    }

    public Integer getPosition() {
        return start + 1;
    }

    public String getTitle() {
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
        Map<String, Supplier<String>> args = getDefaultArguments();
        if (arguments != null) args.putAll(arguments);
        return CommonUtil.formatMessage(template, args);
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
            if (TypeConstants.INTEGER_NUMBER_TYPES.contains(type) ||
                    TypeConstants.DECIMAL_NUMBER_TYPES.contains(type))
                return false;
            return true;
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
                return Alignment.RIGHT;
            return Alignment.LEFT;
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
