package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.model.Alignment;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.IgnoreError;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FixedTypeInfo {
    private Field field;
    private Class<?> type;
    private List<Type> genericTypes = new ArrayList<>();

    private FixedField fixedField;
    private FixedObject fixedObject;

    @Getter(AccessLevel.NONE)
    private String title;
    private String label;
    private Integer start;
    private Integer length;
    private boolean require;
    private Character padding;
    private Alignment alignment;

    private FixedTypeInfo keyInfo;
    private List<FixedTypeInfo> childInfo = new ArrayList<>();

    public FixedTypeInfo(Class<?> type) {
        Assert.notNull(type, "Class Type must not be null!");
        this.type = type;

        this.label = String.format("%s object", type.getName());
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
        this.padding = fixedField.padding() != '\n' ? fixedField.padding() : null;
        this.alignment = fixedField.alignment();

        this.detectGenericTypes();
    }

    public FixedTypeInfo(Object value) {
        Assert.notNull(value, "Object must not be null!");
        this.type = value.getClass();

        this.label = String.format("%s object", type.getName());
        this.detectTypeInfo();
        this.detectFields(type);
    }

    public static FixedTypeInfo of(Class<?> type) {
        return new FixedTypeInfo(type);
    }

    public static FixedTypeInfo of(Field field) {
        return new FixedTypeInfo(field);
    }

    public Integer position() {
        return start + 1;
    }

    public String buildMessage(String template) {
        Assert.hasLength(template, "Title template can't be null.");
        return template
                .replaceAll("\\{label\\}", label)
                .replaceAll("\\{start\\}", start + "")
                .replaceAll("\\{position\\}", position() + "")
                .replaceAll("\\{length\\}", length + "");
    }

    public String title() {
        if (this.title == null)
            this.title = buildMessage("{label} at position {position} and length {length}");
        return this.title;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (field != null) {
            T t = field.getAnnotation(annotationClass);
            if (t != null) return t;
        }
        return type.getAnnotation(annotationClass);
    }

    private void detectGenericTypes() {
        if (field != null) {
            IgnoreError.execute(() -> {
                ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
                for (Type t : integerListType.getActualTypeArguments()) {
                    this.genericTypes.add(t);
                }
            });
        }
    }

    private void detectFields(Class<?> type) {
        FixedObjectHelper
                .getFixedFields(type)
                .stream()
                .map(f -> of(f))
                .forEach(i -> this.childInfo.add(i));
    }

    public FixedTypeInfo findField(String name) {
        for (FixedTypeInfo info : this.childInfo) {
            Field f = info.getField();
            if (f != null && f.getName().equals(name))
                return info;
        }
        return null;
    }

    public void detectTypeInfo() {
        this.fixedObject = type.getAnnotation(FixedObject.class);
        if (fixedObject != null) {
            if (start == null) start = 0;
            if (length == null) {
                length = fixedObject.length();
                Assert.isTrue(length >= 0, String.format("Object length of %s can't less than 0.", this.label));
            }
        }
    }

    public Class<?> detectType(StringAssembler assembler) {
        if (!type.isPrimitive() && fixedObject != null) {
            this.type = FixedObjectHelper.detectType(assembler, type);
            if (field == null)
                this.label = String.format("%s object", type.getName());
            this.detectTypeInfo();
            this.detectFields(type);
        }
        return this.type;
    }
}
