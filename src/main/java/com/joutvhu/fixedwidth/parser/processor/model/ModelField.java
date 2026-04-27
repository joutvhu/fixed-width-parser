package com.joutvhu.fixedwidth.parser.processor.model;

import javax.lang.model.type.TypeMirror;

/**
 * Compile-time model of a single {@code @FixedField}-annotated field
 * within a {@code @FixedObject} class. Extracted from the AST by
 * {@link ModelClass#from}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class ModelField {
    private final String fieldName;
    private final TypeMirror fieldType;
    private final String getterName;
    private final String setterName;
    private final boolean hasGetter;
    private final boolean hasSetter;

    public ModelField(String fieldName, TypeMirror fieldType,
                      String getterName, String setterName,
                      boolean hasGetter, boolean hasSetter) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.getterName = getterName;
        this.setterName = setterName;
        this.hasGetter = hasGetter;
        this.hasSetter = hasSetter;
    }

    public String getFieldName() {
        return fieldName;
    }

    public TypeMirror getFieldType() {
        return fieldType;
    }

    public String getGetterName() {
        return getterName;
    }

    public String getSetterName() {
        return setterName;
    }

    public boolean hasGetter() {
        return hasGetter;
    }

    public boolean hasSetter() {
        return hasSetter;
    }

    /**
     * Whether this field can participate in the generated accessor.
     * Both getter and setter must be present.
     */
    public boolean isAccessible() {
        return hasGetter && hasSetter;
    }
}
