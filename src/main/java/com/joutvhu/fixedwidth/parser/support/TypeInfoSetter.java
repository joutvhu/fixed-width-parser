package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.util.*;
import lombok.Getter;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class TypeInfoSetter extends TypeDetector {
    protected String name;
    protected String label;
    protected Integer start = 0;
    protected Integer length = 0;
    protected boolean require = false;
    protected Character padding;
    protected Alignment alignment = Alignment.AUTO;

    protected List<FixedTypeInfo> elementTypeInfo = new ArrayList<>();
    protected List<FixedTypeInfo> genericTypeInfo = new ArrayList<>();

    public TypeInfoSetter(Class<?> type) {
        super(type);

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            require = true;
        this.detectTypeInfo();
    }

    public TypeInfoSetter(AnnotatedType annotatedType) {
        super(annotatedType);

        this.name = CommonUtil.isNotBlank(fixedParam.label()) ?
                fixedParam.label() : annotatedType.getType().getTypeName();
        this.label = name + " param";

        if (fixedParam.length() >= 0)
            this.length = fixedParam.length();

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            require = true;
        this.padding = fixedParam.padding();
        this.alignment = fixedParam.alignment();
    }

    public TypeInfoSetter(Field field) {
        super(field);

        this.name = CommonUtil.isNotBlank(fixedField.label()) ?
                fixedField.label() : field.getName();
        this.label = name + " field";

        Assert.isTrue(fixedField.start() >= 0, "Start position of field can't less than 0.");
        this.start = fixedField.start();
        Assert.isTrue(fixedField.length() >= 0, "Length of field can't less than 0.");
        this.length = fixedField.length();

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            require = true;
        else
            this.require = fixedField.require();
        this.padding = fixedField.padding();
        this.alignment = fixedField.alignment();
    }

    public TypeInfoSetter(Object value) {
        super(value);

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            require = true;
        this.detectTypeInfo();
    }

    @Override
    public void afterInit() {
    }

    private void detectTypeInfo() {
        this.name = CommonUtil.isNotBlank(fixedObject.label()) ?
                fixedObject.label() : type.getName();
        this.label = name + " object";

        Assert.isTrue(fixedObject.length() >= 0, "Length of object can't less than 0.");
        this.length = fixedObject.length();
    }

    @Override
    public void detectedNewType(Class<?> newType) {
        FixedObject fixedObject = type.getAnnotation(FixedObject.class);
        if (fixedObject != null) {
            this.fixedObject = fixedObject;
            Assert.isTrue(fixedObject.length() >= 0, "Length of object can't less than 0.");
            if (TypeConstants.NOT_NULL_TYPES.contains(type))
                require = true;
            this.detectTypeInfo();
        }
    }

    @Override
    public void afterTypeDetected() {
        this.detectFields(type);
        if (SourceType.FIELD_TYPE.equals(this.getSourceType()))
            this.detectGenericTypes();
    }

    private void detectFields(Class<?> type) {
        FixedHelper
                .getFixedFields(type)
                .stream()
                .map(FixedTypeInfo::of)
                .forEach(i -> this.elementTypeInfo.add(i));
    }

    private void detectGenericTypes() {
        if (field != null) {
            AnnotatedType[] annotatedTypes = ReflectionUtil.getAnnotatedActualTypeArguments(field);
            for (AnnotatedType annotatedType : CommonUtil.defaultIfNull(annotatedTypes, new AnnotatedType[0]))
                this.genericTypeInfo.add(new FixedTypeInfo(annotatedType));
        }
    }
}
