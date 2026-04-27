package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedPadding;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.annotation.FixedRequired;
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
 * Refactored to be more immutable and thread-safe.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Getter
public abstract class TypeInfoSetter extends TypeDetector {
    protected final String name;
    protected final String label;
    protected final Integer start;
    protected final Integer length;
    protected final boolean require;
    protected final Character padding;
    protected final Character nullPadding;
    protected final KeepPadding keepPadding;
    protected final Alignment alignment;

    protected final List<FixedTypeInfo> elementTypeInfo;
    protected final List<FixedTypeInfo> genericTypeInfo;

    protected TypeInfoSetter(Class<?> type) {
        super(type);
        this.name = CommonUtil.isNotBlank(fixedObject.label()) ? fixedObject.label() : type.getName();
        this.label = name + " object";
        this.start = 0;
        this.length = fixedObject.length();
        this.require = TypeConstants.NOT_NULL_TYPES.contains(type);
        this.padding = fixedObject.padding();
        this.nullPadding = fixedObject.nullPadding();
        this.keepPadding = fixedObject.keepPadding();
        this.alignment = fixedObject.alignment();

        this.elementTypeInfo = Collections.unmodifiableList(this.detectFields(type));
        this.genericTypeInfo = Collections.unmodifiableList(this.detectGenericTypes());
    }

    protected TypeInfoSetter(AnnotatedType annotatedType) {
        super(annotatedType);
        this.name = CommonUtil.isNotBlank(fixedParam.label()) ?
                fixedParam.label() : annotatedType.getType().getTypeName();
        this.label = name + " param";
        this.start = 0;
        this.length = fixedParam.length() >= 0 ? fixedParam.length() : 0;
        this.require = TypeConstants.NOT_NULL_TYPES.contains(type);
        this.padding = fixedParam.padding();
        this.nullPadding = fixedParam.nullPadding();
        this.keepPadding = fixedParam.keepPadding();
        this.alignment = fixedParam.alignment();

        this.elementTypeInfo = Collections.unmodifiableList(this.detectFields(type));
        this.genericTypeInfo = Collections.unmodifiableList(this.detectGenericTypes());
    }

    protected TypeInfoSetter(Field field) {
        super(field);
        this.name = CommonUtil.isNotBlank(fixedField.label()) ?
                fixedField.label() : field.getName();
        this.label = name + " field";
        this.start = fixedField.start();
        this.length = fixedField.length();

        // ── @FixedRequired / @FixedField(required) ────────────────────────────
        // Precedence: @FixedRequired on field > @FixedField(required) > NOT_NULL_TYPES
        FixedRequired fixedRequired = getAnnotation(FixedRequired.class);
        this.require = TypeConstants.NOT_NULL_TYPES.contains(type)
                || fixedRequired != null
                || fixedField.required();

        // ── @FixedPadding precedence ──────────────────────────────────────────
        // 1. @FixedPadding on field
        // 2. @FixedField(padding/nullPadding/keepPadding/alignment) — backward compat
        // 3. @FixedPadding on class (default for all fields in the class)
        FixedPadding fieldPadding = getAnnotation(FixedPadding.class);
        FixedPadding classPadding = field.getDeclaringClass().getAnnotation(FixedPadding.class);

        if (fieldPadding != null) {
            // Field-level @FixedPadding wins
            this.padding = fieldPadding.value();
            this.nullPadding = fieldPadding.nullValue();
            this.keepPadding = fieldPadding.keep();
            this.alignment = fieldPadding.alignment();
        } else if (fixedField.padding() != Padding.AUTO
                || fixedField.nullPadding() != Padding.AUTO
                || fixedField.keepPadding() != KeepPadding.AUTO
                || fixedField.alignment() != Alignment.AUTO) {
            // @FixedField attributes explicitly set — backward compat
            this.padding = fixedField.padding();
            this.nullPadding = fixedField.nullPadding();
            this.keepPadding = fixedField.keepPadding();
            this.alignment = fixedField.alignment();
        } else if (classPadding != null) {
            // Class-level @FixedPadding as default
            this.padding = classPadding.value();
            this.nullPadding = classPadding.nullValue();
            this.keepPadding = classPadding.keep();
            this.alignment = classPadding.alignment();
        } else {
            this.padding = fixedField.padding();
            this.nullPadding = fixedField.nullPadding();
            this.keepPadding = fixedField.keepPadding();
            this.alignment = fixedField.alignment();
        }

        this.elementTypeInfo = Collections.unmodifiableList(this.detectFields(type));
        this.genericTypeInfo = Collections.unmodifiableList(this.detectGenericTypes());
    }

    /**
     * Internal constructor for subtype creation
     */
    protected TypeInfoSetter(Class<?> type, String name, String label, Integer start, Integer length, boolean require,
                           Character padding, Character nullPadding, KeepPadding keepPadding, Alignment alignment,
                           Field field, AnnotatedType annotatedType, FixedField fixedField, FixedParam fixedParam,
                           FixedObject fixedObject, SourceType sourceType, boolean finalType) {
        super(type, field, annotatedType, fixedField, fixedParam, fixedObject, sourceType, finalType);
        this.name = name;
        this.label = label;
        this.start = start;
        this.length = length;
        this.require = require;
        this.padding = padding;
        this.nullPadding = nullPadding;
        this.keepPadding = keepPadding;
        this.alignment = alignment;

        this.elementTypeInfo = Collections.unmodifiableList(this.detectFields(type));
        this.genericTypeInfo = Collections.unmodifiableList(this.detectGenericTypes());
    }

    protected List<FixedTypeInfo> detectFields(Class<?> type) {
        List<FixedTypeInfo> fields = new ArrayList<>();
        this.getFixedFields(type)
                .stream()
                .map(FixedMetadataRegistry::get)
                .filter(info -> info != null)
                .forEach(fields::add);
        return fields;
    }

    protected List<FixedTypeInfo> detectGenericTypes() {
        List<FixedTypeInfo> genericTypes = new ArrayList<>();
        List<AnnotatedType> annotatedTypes = new ArrayList<>();

        if (field != null) {
            AnnotatedType[] annotatedTypesArray = ReflectionUtil.getAnnotatedActualTypeArguments(field);
            if (CommonUtil.isNotBlank(annotatedTypesArray))
                Collections.addAll(annotatedTypes, annotatedTypesArray);
        }

        if (annotatedType != null && annotatedType instanceof AnnotatedParameterizedType) {
            AnnotatedType[] annotatedTypesArray = ReflectionUtil
                    .getAnnotatedActualTypeArguments((AnnotatedParameterizedType) annotatedType);
            if (CommonUtil.isNotBlank(annotatedTypesArray))
                Collections.addAll(annotatedTypes, annotatedTypesArray);
        }

        for (AnnotatedType at : annotatedTypes)
            genericTypes.add(FixedMetadataRegistry.get(at));
        return genericTypes;
    }

    @Override
    public void afterTypeDetected() {
        // No-op in new immutable design
    }
}
