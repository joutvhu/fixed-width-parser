package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.Padding;
import com.joutvhu.fixedwidth.parser.util.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FixedTypeInfo implements TypeInfo {
    private Field field;
    private Class<?> type;
    private AnnotatedType annotatedType;

    private FixedField fixedField;
    private FixedParam fixedParam;
    private FixedObject fixedObject;

    @Getter(AccessLevel.NONE)
    private String title;
    private String label;
    private Integer start;
    private Integer length;
    private boolean require;
    private Character padding;
    private Alignment alignment;

    private List<FixedTypeInfo> elementTypeInfo = new ArrayList<>();
    private List<FixedTypeInfo> genericTypeInfo = new ArrayList<>();

    public FixedTypeInfo(Class<?> type) {
        Assert.notNull(type, "Class Type must not be null!");
        this.type = type;

        this.label = String.format("%s object", type.getName());
        this.detectTypeInfo();
    }

    public FixedTypeInfo(AnnotatedType annotatedType) {
        Type t = annotatedType.getType();
        this.fixedParam = annotatedType.getAnnotation(FixedParam.class);
        Assert.isTrue(t instanceof Class, String
                .format("Generic type %s of %s is not a class.", t.getTypeName(), label));
        Assert.notNull(fixedParam, String
                .format("Generic type %s of %s must be annotated with FixedParam.", t.getTypeName(), label));
        this.annotatedType = annotatedType;

        this.type = (Class<?>) t;
        this.label = String.format("%s object", type.getName());
        this.start = 0;
        this.length = fixedParam.length();
        this.padding = fixedParam.padding();
        this.alignment = fixedParam.alignment();

        this.detectTypeInfo();
    }

    public FixedTypeInfo(Field field) {
        Assert.notNull(field, "Field must not be null!");
        this.field = field;
        this.type = field.getType();

        this.fixedField = field.getAnnotation(FixedField.class);
        this.fixedObject = type.getAnnotation(FixedObject.class);
        Assert.notNull(fixedField, String.format("The %s is not a fixed field.", field.getName()));

        this.label = String.format("%s field", fixedField != null ? fixedField.label() : field.getName());
        this.start = fixedField.start();
        Assert.isTrue(start >= 0, String.format("Start position of %s can't less than 0.", this.label));
        this.length = fixedField.length();
        Assert.isTrue(length >= 0, String.format("Field length of %s can't less than 0.", this.label));
        this.require = fixedField.require();
        this.padding = fixedField.padding();
        this.alignment = fixedField.alignment();

        this.detectGenericTypes();
    }

    public FixedTypeInfo(Object value) {
        Assert.notNull(value, "Object must not be null!");
        this.type = value.getClass();

        this.label = String.format("%s object", type.getName());
        this.afterTypeDetected();
    }

    public static FixedTypeInfo of(Class<?> type) {
        return new FixedTypeInfo(type);
    }

    public static FixedTypeInfo of(AnnotatedType annotatedType) {
        return new FixedTypeInfo(annotatedType);
    }

    public static FixedTypeInfo of(Field field) {
        return new FixedTypeInfo(field);
    }

    public static FixedTypeInfo of(Object value) {
        return new FixedTypeInfo(value);
    }

    public Integer position() {
        return start + 1;
    }

    public String buildMessage(String template, Object... arguments) {
        Assert.hasLength(template, "Title template can't be null.");
        template = template
                .replaceAll("\\{label\\}", label)
                .replaceAll("\\{start\\}", start + "")
                .replaceAll("\\{position\\}", position() + "")
                .replaceAll("\\{length\\}", length + "")
                .replaceAll("\\{title\\}", getTitle());
        return MessageFormat.format(template, arguments);
    }

    public String getTitle() {
        if (this.title == null)
            this.title = buildMessage("{label} at position {position} and length {length}");
        return this.title;
    }

    public char defaultPadding() {
        if (padding == null) return ' ';
        if (Padding.AUTO != padding) return padding;
        if ((TypeConstants.INTEGER_NUMBER_TYPES.contains(type) || TypeConstants.DECIMAL_NUMBER_TYPES.contains(type)) &&
                (alignment == null || Alignment.AUTO == alignment || Alignment.LEFT == alignment))
            return '0';
        return ' ';
    }

    public Alignment defaultAlignment() {
        if (alignment != null && Alignment.AUTO != alignment) return alignment;
        if (TypeConstants.INTEGER_NUMBER_TYPES.contains(type) || TypeConstants.DECIMAL_NUMBER_TYPES.contains(type))
            return Alignment.LEFT;
        return Alignment.RIGHT;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return ReflectionUtil.getAnnotation(annotationClass, field, annotatedType, type);
    }

    public FixedTypeInfo findField(String name) {
        for (FixedTypeInfo info : this.elementTypeInfo) {
            Field f = info.getField();
            if (f != null && f.getName().equals(name))
                return info;
        }
        return null;
    }

    private void detectGenericTypes() {
        if (field != null) {
            AnnotatedType[] annotatedTypes = ReflectionUtil.getAnnotatedActualTypeArguments(field);
            for (AnnotatedType annotatedType : CommonUtil.defaultIfNull(annotatedTypes, new AnnotatedType[0]))
                this.genericTypeInfo.add(of(annotatedType));
        }
    }

    private void detectFields(Class<?> type) {
        FixedHelper
                .getFixedFields(type)
                .stream()
                .map(FixedTypeInfo::of)
                .forEach(i -> this.elementTypeInfo.add(i));
    }

    private void detectTypeInfo() {
        this.fixedObject = type.getAnnotation(FixedObject.class);
        if (fixedObject != null && fixedParam == null) {
            if (start == null) start = 0;
            if (length == null) {
                length = fixedObject.length();
                Assert.isTrue(length >= 0, String.format("Object length of %s can't less than 0.", this.label));
            }
        }
    }

    @Override
    public Class<?> detectTypeWith(StringAssembler assembler) {
        if (!type.isPrimitive() && fixedObject != null) {
            this.type = FixedHelper.detectType(assembler, type);
            if (field == null)
                this.label = String.format("%s object", type.getName());
        }
        this.afterTypeDetected();
        return this.type;
    }

    @Override
    public void afterTypeDetected() {
        this.detectTypeInfo();
        this.detectFields(type);
    }
}
