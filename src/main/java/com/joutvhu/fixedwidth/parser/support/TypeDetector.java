package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Detect subtype and {@link FixedField}, {@link FixedParam} and {@link FixedObject} annotation
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Getter
public abstract class TypeDetector {
    protected Field field;
    protected Class<?> type;
    protected AnnotatedType annotatedType;
    protected ParameterizedType parameterizedType;

    protected FixedField fixedField;
    protected FixedParam fixedParam;
    protected FixedObject fixedObject;

    private boolean finalType = false;
    private SourceType sourceType;

    protected TypeDetector(Class<?> type) {
        this.beforeInit();
        Assert.notNull(type, "Class Type must not be null!");
        this.type = type;

        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.notNull(fixedObject, String.format("The %s class must be annotated with FixedObject.", type.getName()));
        Assert.isTrue(fixedObject.length() >= 0, "Length of object can't less than 0.");

        this.sourceType = SourceType.CLASS_TYPE;
    }

    protected TypeDetector(AnnotatedType annotatedType) {
        this.beforeInit();
        Assert.notNull(annotatedType, "AnnotatedType must not be null!");
        this.annotatedType = annotatedType;

        Type t = annotatedType.getType();
        if (t instanceof ParameterizedType) {
            this.parameterizedType = (ParameterizedType) t;
            t = parameterizedType.getRawType();
        }

        Assert.isTrue(t instanceof Class, String
                .format("The %s type is not a class.", t.getTypeName()));
        this.type = (Class<?>) t;

        this.fixedParam = getAnnotation(FixedParam.class);
        Assert.notNull(fixedParam, String.format("The %s type must be annotated with FixedParam.", t.getTypeName()));
        Assert.isTrue(fixedParam.length() >= 0, "Length of param can't less than 0.");
        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.isTrue(fixedObject == null || fixedObject.length() >= 0, "Length of object can't less than 0.");

        this.sourceType = SourceType.PARAM_TYPE;
    }

    protected TypeDetector(Field field) {
        this.beforeInit();
        Assert.notNull(field, "Field must not be null!");
        this.field = field;
        this.type = field.getType();

        this.fixedField = getAnnotation(FixedField.class);
        Assert.notNull(fixedField, String.format("The %s field must be annotated with FixedField.", field.getName()));
        this.fixedObject = getAnnotation(FixedObject.class);

        this.sourceType = SourceType.FIELD_TYPE;
    }

    protected TypeDetector(Object value) {
        this.beforeInit();
        Assert.notNull(value, "Object must not be null!");
        this.type = value.getClass();

        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.notNull(fixedObject, String.format("The %s class must be annotated with FixedObject.", type.getName()));

        this.finalType = true;
        this.sourceType = SourceType.OBJECT_TYPE;
    }

    protected <T extends TypeDetector> T postConstruct() {
        this.afterInit();
        if (finalType)
            this.afterTypeDetected();
        return (T) this;
    }

    public void beforeInit() {
        // Virtual method.
    }

    public void afterInit() {
        // Virtual method.
    }

    /**
     * Find an annotation from field, annotatedType, type by annotation class
     *
     * @param annotationClass the Class object corresponding to the annotation type
     * @param <T>             the type of the annotation to query for and return if present
     * @return annotation for the specified annotation type
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return ReflectionUtil.getAnnotation(annotationClass, field, annotatedType, type);
    }

    /**
     * Use {@link StringAssembler} to find final type
     *
     * @param assembler is {@link StringAssembler} of input string
     * @return final class type
     */
    public final Class<?> detectTypeWith(StringAssembler assembler) {
        if (!finalType) {
            if (!type.isPrimitive() && fixedObject != null) {
                Class<?> newType = FixedHelper.detectType(assembler, type);
                if (!type.equals(newType)) {
                    this.detectedNewType(newType);
                    this.type = newType;
                }
            }
            this.afterTypeDetected();
        }
        return this.type;
    }

    /**
     * Use value object to get final type
     *
     * @param value object
     * @return final class type
     */
    public final Class<?> detectTypeWith(Object value) {
        if (!finalType) {
            if (value != null) {
                Class<?> newType = value.getClass();
                if (!type.equals(newType)) {
                    this.detectedNewType(newType);
                    this.type = newType;
                }
            }
            this.afterTypeDetected();
        }
        return this.type;
    }

    /**
     * Called when class type changing
     *
     * @param newType new class type
     */
    public void detectedNewType(Class<?> newType) {
        // Virtual method.
    }

    /**
     * After final class type detected
     */
    public abstract void afterTypeDetected();

    enum SourceType {
        CLASS_TYPE,
        FIELD_TYPE,
        PARAM_TYPE,
        OBJECT_TYPE
    }
}
