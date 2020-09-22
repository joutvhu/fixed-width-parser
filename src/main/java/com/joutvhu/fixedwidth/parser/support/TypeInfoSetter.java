package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.domain.Padding;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import com.joutvhu.fixedwidth.parser.util.TypeConstants;
import lombok.Getter;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Find and save type info, annotation info
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Getter
public abstract class TypeInfoSetter extends TypeDetector {
    protected String name;
    protected String label;
    protected Integer start = 0;
    protected Integer length = 0;
    protected boolean require = false;
    protected Character padding = Padding.AUTO;
    protected Character nullPadding = Padding.AUTO;
    protected KeepPadding keepPadding = KeepPadding.AUTO;
    protected Alignment alignment = Alignment.AUTO;

    protected List<FixedTypeInfo> elementTypeInfo = new ArrayList<>();
    protected List<FixedTypeInfo> genericTypeInfo = new ArrayList<>();

    protected TypeInfoSetter(Class<?> type) {
        super(type);

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            this.require = true;
        this.detectTypeInfo();
    }

    protected TypeInfoSetter(AnnotatedType annotatedType) {
        super(annotatedType);

        this.name = CommonUtil.isNotBlank(fixedParam.label()) ?
                fixedParam.label() : annotatedType.getType().getTypeName();
        this.label = name + " param";

        if (fixedParam.length() >= 0)
            this.length = fixedParam.length();

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            this.require = true;
        this.padding = fixedParam.padding();
        this.nullPadding = fixedParam.nullPadding();
        this.keepPadding = fixedParam.keepPadding();
        this.alignment = fixedParam.alignment();
    }

    protected TypeInfoSetter(Field field) {
        super(field);

        this.name = CommonUtil.isNotBlank(fixedField.label()) ?
                fixedField.label() : field.getName();
        this.label = name + " field";

        Assert.isTrue(fixedField.start() >= 0, "Start position of field cannot less than 0.");
        this.start = fixedField.start();
        Assert.isTrue(fixedField.length() >= 0, "Length of field cannot less than 0.");
        this.length = fixedField.length();

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            this.require = true;
        else
            this.require = fixedField.require();
        this.padding = fixedField.padding();
        this.nullPadding = fixedField.nullPadding();
        this.keepPadding = fixedField.keepPadding();
        this.alignment = fixedField.alignment();
    }

    protected TypeInfoSetter(Object value) {
        super(value);

        if (TypeConstants.NOT_NULL_TYPES.contains(type))
            this.require = true;
        this.detectTypeInfo();
    }

    /**
     * Detect type information of {@link FixedObject}
     */
    private void detectTypeInfo() {
        this.name = CommonUtil.isNotBlank(fixedObject.label()) ? fixedObject.label() : type.getName();
        this.label = name + " object";

        Assert.isTrue(fixedObject.length() >= 0, "Length of object cannot less than 0.");
        this.length = fixedObject.length();

        if (SourceType.FIELD_TYPE.equals(getSourceType()) || SourceType.PARAM_TYPE.equals(getSourceType())) {
            this.padding = fixedObject.padding();
            this.nullPadding = fixedObject.nullPadding();
            this.keepPadding = fixedObject.keepPadding();
            this.alignment = fixedObject.alignment();
        } else {
            if (padding == null || Padding.AUTO == padding)
                this.padding = fixedObject.padding();
            if (nullPadding == null || Padding.AUTO == nullPadding)
                this.nullPadding = fixedObject.nullPadding();
            if (keepPadding == null || KeepPadding.AUTO.equals(keepPadding))
                this.keepPadding = fixedObject.keepPadding();
            if (alignment == null || Alignment.AUTO.equals(alignment))
                this.alignment = fixedObject.alignment();
        }
    }

    /**
     * Called when class type changing
     *
     * @param newType new class type
     */
    @Override
    public void detectedNewType(Class<?> newType) {
        FixedObject fixedObject = type.getAnnotation(FixedObject.class);
        if (fixedObject != null) {
            this.fixedObject = fixedObject;
            Assert.isTrue(fixedObject.length() >= 0, "Length of object cannot less than 0.");
            if (TypeConstants.NOT_NULL_TYPES.contains(type))
                this.require = true;
            this.detectTypeInfo();
        }
    }

    /**
     * After final class type detected
     */
    @Override
    public void afterTypeDetected() {
        this.detectFields(type);
        this.detectGenericTypes();
    }

    /**
     * Find all field annotated by {@link FixedField}
     *
     * @param type
     */
    private void detectFields(Class<?> type) {
        this.elementTypeInfo = new ArrayList<>();
        this.getFixedFields(type)
                .stream()
                .map(FixedTypeInfo::of)
                .forEach(i -> this.elementTypeInfo.add(i));
    }

    /**
     * Find all generic types.
     */
    private void detectGenericTypes() {
        this.genericTypeInfo = new ArrayList<>();
        List<AnnotatedType> annotatedTypes = new ArrayList<>();

        if (field != null) {
            AnnotatedType[] annotatedTypesArray = ReflectionUtil
                    .getAnnotatedActualTypeArguments(field);
            if (CommonUtil.isNotBlank(annotatedTypesArray))
                Collections.addAll(annotatedTypes, annotatedTypesArray);
        }

        if (annotatedType != null && annotatedType instanceof AnnotatedParameterizedType) {
            AnnotatedType[] annotatedTypesArray = ReflectionUtil
                    .getAnnotatedActualTypeArguments((AnnotatedParameterizedType) annotatedType);
            if (CommonUtil.isNotBlank(annotatedTypesArray))
                Collections.addAll(annotatedTypes, annotatedTypesArray);
        }

        for (AnnotatedType annotatedType : annotatedTypes)
            this.genericTypeInfo.add(FixedTypeInfo.of(annotatedType));
    }
}
