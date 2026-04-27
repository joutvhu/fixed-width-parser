package com.joutvhu.fixedwidth.parser.processor.model;

import com.joutvhu.fixedwidth.parser.processor.util.ProcessorUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Compile-time model of a single {@code @FixedObject}-annotated class.
 * Built from the annotation processing AST by {@link #from}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class ModelClass {

    private static final String FIXED_FIELD_ANNOTATION =
        "com.joutvhu.fixedwidth.parser.annotation.FixedField";

    private final TypeElement typeElement;
    private final String packageName;
    private final String simpleClassName;
    private final String qualifiedClassName;
    private final List<ModelField> fields;

    private ModelClass(TypeElement typeElement, String packageName,
                       String simpleClassName, String qualifiedClassName,
                       List<ModelField> fields) {
        this.typeElement = typeElement;
        this.packageName = packageName;
        this.simpleClassName = simpleClassName;
        this.qualifiedClassName = qualifiedClassName;
        this.fields = Collections.unmodifiableList(fields);
    }

    /**
     * Extracts a {@link ModelClass} from a {@link TypeElement} discovered
     * by the annotation processor.
     */
    public static ModelClass from(TypeElement typeElement, ProcessingEnvironment env) {
        String qualifiedName = typeElement.getQualifiedName().toString();
        String packageName = "";
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = qualifiedName.substring(0, lastDot);
        }
        String simpleName = typeElement.getSimpleName().toString();

        List<Element> annotatedFields = ProcessorUtil.getAnnotatedFields(
            typeElement, FIXED_FIELD_ANNOTATION, env);

        List<ModelField> modelFields = new ArrayList<>();
        for (Element fieldElement : annotatedFields) {
            String fieldName = fieldElement.getSimpleName().toString();
            TypeMirror fieldType = fieldElement.asType();

            // Determine getter name (isXxx for boolean, getXxx otherwise)
            String getterName;
            if (fieldType.getKind() == TypeKind.BOOLEAN) {
                getterName = "is" + ProcessorUtil.capitalize(fieldName);
            } else {
                getterName = "get" + ProcessorUtil.capitalize(fieldName);
            }
            String setterName = "set" + ProcessorUtil.capitalize(fieldName);

            boolean hasGetter = ProcessorUtil.hasMethod(getterName, typeElement, env);
            boolean hasSetter = ProcessorUtil.hasSetter(setterName, typeElement, env);

            // Also try getXxx for boolean fields if isXxx not found
            if (!hasGetter && fieldType.getKind() == TypeKind.BOOLEAN) {
                getterName = "get" + ProcessorUtil.capitalize(fieldName);
                hasGetter = ProcessorUtil.hasMethod(getterName, typeElement, env);
            }

            // Phase 2: Metadata extraction
            com.joutvhu.fixedwidth.parser.annotation.FixedField fixedField = 
                fieldElement.getAnnotation(com.joutvhu.fixedwidth.parser.annotation.FixedField.class);
            
            Integer start = fixedField != null ? fixedField.start() : 0;
            Integer length = fixedField != null ? fixedField.length() : 0;
            
            boolean require = fieldType.getKind().isPrimitive() || 
                fieldElement.getAnnotation(com.joutvhu.fixedwidth.parser.annotation.FixedRequired.class) != null;
            
            com.joutvhu.fixedwidth.parser.annotation.FixedPadding fieldPadding = 
                fieldElement.getAnnotation(com.joutvhu.fixedwidth.parser.annotation.FixedPadding.class);
            com.joutvhu.fixedwidth.parser.annotation.FixedPadding classPadding = 
                typeElement.getAnnotation(com.joutvhu.fixedwidth.parser.annotation.FixedPadding.class);
            
            Character padding = null;
            Character nullPadding = null;
            String keepPadding = null;
            String alignment = null;
            
            if (fieldPadding != null) {
                padding = fieldPadding.value();
                nullPadding = fieldPadding.nullValue();
                keepPadding = fieldPadding.keep().name();
                alignment = fieldPadding.alignment().name();
            } else if (classPadding != null) {
                padding = classPadding.value();
                nullPadding = classPadding.nullValue();
                keepPadding = classPadding.keep().name();
                alignment = classPadding.alignment().name();
            }

            modelFields.add(new ModelField(
                fieldName, fieldType, getterName, setterName,
                hasGetter, hasSetter, fieldElement,
                start, length, require, padding, nullPadding, keepPadding, alignment));
        }

        return new ModelClass(typeElement, packageName, simpleName,
            qualifiedName, modelFields);
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public List<ModelField> getFields() {
        return fields;
    }

    /**
     * Returns only the fields that have both getter and setter,
     * i.e. fields that can participate in the generated accessor.
     */
    public List<ModelField> getAccessibleFields() {
        List<ModelField> result = new ArrayList<>();
        for (ModelField f : fields) {
            if (f.isAccessible()) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * The simple name of the generated accessor class.
     */
    public String getAccessorSimpleName() {
        return simpleClassName + "$FixedAccessor";
    }
}
