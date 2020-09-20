package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
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

    public String buildMessage(String template, Object... arguments) {
        Assert.hasLength(template, "The template message can't be black.");
        template = CommonUtil.replaceAll(template, "{label}", () -> label);
        template = CommonUtil.replaceAll(template, "{start}", () -> start + "");
        template = CommonUtil.replaceAll(template, "{position}", () -> getPosition() + "");
        template = CommonUtil.replaceAll(template, "{length}", () -> length + "");
        template = CommonUtil.replaceAll(template, "{title}", this::getTitle);
        return MessageFormat.format(template, arguments);
    }

    public char getDefaultPadding() {
        if (padding == null) return ' ';
        if (Padding.AUTO != padding) return padding;
        if ((TypeConstants.INTEGER_NUMBER_TYPES.contains(type) || TypeConstants.DECIMAL_NUMBER_TYPES.contains(type)) &&
                (alignment == null || Alignment.AUTO == alignment || Alignment.LEFT == alignment))
            return '0';
        return ' ';
    }

    public Alignment getDefaultAlignment() {
        if (alignment != null && Alignment.AUTO != alignment) return alignment;
        if (TypeConstants.INTEGER_NUMBER_TYPES.contains(type) || TypeConstants.DECIMAL_NUMBER_TYPES.contains(type))
            return Alignment.LEFT;
        return Alignment.RIGHT;
    }

    public <T> T getAnnotationValue(Class<? extends Annotation> annotationClass, String name, Class<T> type) {
        Annotation annotation = getAnnotation(annotationClass);
        if (annotation != null)
            return ReflectionUtil.getValueFromAnnotation(name, type, annotation);
        return ReflectionUtil.getDefaultValueOfAnnotation(name, type, annotationClass);
    }

    public boolean isAssignableTo(Class<?> cls) {
        return cls != null && cls.isAssignableFrom(type);
    }

    public boolean isOneOfTypes(List<Class<?>> classes) {
        return classes != null && classes.contains(type);
    }
}
