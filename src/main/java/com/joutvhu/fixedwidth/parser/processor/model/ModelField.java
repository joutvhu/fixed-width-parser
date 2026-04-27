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

    // Phase 3: Provide raw element for validation
    private final javax.lang.model.element.Element element;

    // Phase 2: Metadata pre-computation
    private final Integer start;
    private final Integer length;
    private final boolean require;
    private final Character padding;
    private final Character nullPadding;
    private final String keepPadding;
    private final String alignment;

    public ModelField(String fieldName, TypeMirror fieldType,
                      String getterName, String setterName,
                      boolean hasGetter, boolean hasSetter,
                      javax.lang.model.element.Element element,
                      Integer start, Integer length, boolean require,
                      Character padding, Character nullPadding, String keepPadding, String alignment) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.getterName = getterName;
        this.setterName = setterName;
        this.hasGetter = hasGetter;
        this.hasSetter = hasSetter;
        this.element = element;
        this.start = start;
        this.length = length;
        this.require = require;
        this.padding = padding;
        this.nullPadding = nullPadding;
        this.keepPadding = keepPadding;
        this.alignment = alignment;
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

    public javax.lang.model.element.Element getElement() {
        return element;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getLength() {
        return length;
    }

    public boolean isRequire() {
        return require;
    }

    public Character getPadding() {
        return padding;
    }

    public Character getNullPadding() {
        return nullPadding;
    }

    public String getKeepPadding() {
        return keepPadding;
    }

    public String getAlignment() {
        return alignment;
    }

    /**
     * Whether this field can participate in the generated accessor.
     * Both getter and setter must be present.
     */
    public boolean isAccessible() {
        return hasGetter && hasSetter;
    }
}
