package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.model.Alignment;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FixedTypeInfo {
    private Field field;
    private Class<?> type;

    private FixedField fixedField;
    private FixedObject fixedObject;

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
        this.require = fixedField.require();
        this.padding = fixedField.padding() != '\n' ? fixedField.padding() : null;
        this.alignment = fixedField.alignment();
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
        return buildMessage("{label} at position {position} and length {length}");
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (field != null) {
            T t = field.getAnnotation(annotationClass);
            if (t != null) return t;
        }
        return type.getAnnotation(annotationClass);
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
            if (length == null) length = fixedObject.length();
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

    public String trimValue(StringAssembler assembler) {
        String value = assembler.get(0, length);
        if (padding != null) {
            switch (alignment) {
                case LEFT:
                    return CommonUtil.trimLeftBy(value, padding);
                case RIGHT:
                    return CommonUtil.trimRightBy(value, padding);
                case CENTRE:
                    return CommonUtil.trimBy(value, padding);
                default:
                    return value;
            }
        }
        return value;
    }
}
