package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import lombok.AccessLevel;
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
public abstract class TypeDetector implements FinalTypeFinder {
    @Getter(AccessLevel.NONE)
    private Class<?> rootType;

    protected final Field field;
    protected final Class<?> type;
    protected final AnnotatedType annotatedType;
    protected ParameterizedType parameterizedType;

    protected final FixedField fixedField;
    protected final FixedParam fixedParam;
    protected final FixedObject fixedObject;

    private boolean finalType = false;
    private SourceType sourceType;

    protected TypeDetector(Class<?> type) {
        this.beforeInit();
        Assert.notNull(type, "Class Type must not be null!");
        this.field = null;
        this.annotatedType = null;
        this.type = type;
        this.rootType = type;

        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.notNull(fixedObject, String.format("The %s class must be annotated with FixedObject.", type.getName()));
        Assert.isTrue(fixedObject.length() >= 0, "Length of object cannot less than 0.");

        this.fixedField = null;
        this.fixedParam = null;
        this.sourceType = SourceType.CLASS_TYPE;
    }

    protected TypeDetector(AnnotatedType annotatedType) {
        this.beforeInit();
        Assert.notNull(annotatedType, "AnnotatedType must not be null!");
        this.field = null;
        this.annotatedType = annotatedType;

        Type t = annotatedType.getType();
        ParameterizedType parameterizedType = null;
        if (t instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) t;
            t = parameterizedType.getRawType();
        }
        this.parameterizedType = parameterizedType;

        Assert.isTrue(t instanceof Class, String
            .format("The %s type is not a class.", t.getTypeName()));
        this.type = (Class<?>) t;
        this.rootType = type;

        this.fixedParam = getAnnotation(FixedParam.class);
        Assert.notNull(fixedParam, String.format("The %s type must be annotated with FixedParam.", t.getTypeName()));
        Assert.isTrue(fixedParam.length() >= 0, "Length of param cannot less than 0.");
        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.isTrue(fixedObject == null || fixedObject.length() >= 0, "Length of object cannot less than 0.");

        this.fixedField = null;
        this.sourceType = SourceType.PARAM_TYPE;
    }

    protected TypeDetector(Field field) {
        this.beforeInit();
        Assert.notNull(field, "Field must not be null!");
        this.field = field;
        this.annotatedType = null;
        this.type = field.getType();
        this.rootType = type;

        this.fixedField = getAnnotation(FixedField.class);
        Assert.notNull(fixedField, String.format("The %s field must be annotated with FixedField.", field.getName()));
        this.fixedObject = getAnnotation(FixedObject.class);

        this.fixedParam = null;
        this.sourceType = SourceType.FIELD_TYPE;
    }

    protected TypeDetector(Object value) {
        this.beforeInit();
        Assert.notNull(value, "Object must not be null!");
        this.field = null;
        this.annotatedType = null;
        this.type = value.getClass();
        this.rootType = type;

        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.notNull(fixedObject, String.format("The %s class must be annotated with FixedObject.", type.getName()));

        this.fixedField = null;
        this.fixedParam = null;
        this.finalType = true;
        this.sourceType = SourceType.OBJECT_TYPE;
    }

    /**
     * Internal constructor for subtype creation, preserving context
     */
    protected TypeDetector(Class<?> type, Field field, AnnotatedType annotatedType,
                           FixedField fixedField, FixedParam fixedParam, FixedObject fixedObject,
                           SourceType sourceType, boolean finalType) {
        this.type = type;
        this.rootType = type;
        this.field = field;
        this.annotatedType = annotatedType;
        this.fixedField = fixedField;
        this.fixedParam = fixedParam;
        this.fixedObject = fixedObject;
        this.sourceType = sourceType;
        this.finalType = finalType;
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
     * Find an annotation from field, annotatedType, type by annotation class.
     *
     * <p>Also searches composed annotations (meta-annotations) up to depth 3.
     * For example, if a field has {@code @ZeroPaddedNumber} and that annotation
     * is itself annotated with {@code @FixedPadding}, this method will find
     * {@code @FixedPadding} via composition.
     *
     * @param annotationClass the Class object corresponding to the annotation type
     * @param <T>             the type of the annotation to query for and return if present
     * @return annotation for the specified annotation type
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        // Direct lookup first
        T direct = ReflectionUtil.getAnnotation(annotationClass, field, annotatedType, type);
        if (direct != null) return direct;

        // Composed annotation lookup — search meta-annotations on field annotations
        if (field != null) {
            T composed = findComposedAnnotation(annotationClass, field.getAnnotations(), 1);
            if (composed != null) return composed;
        }
        if (annotatedType != null) {
            T composed = findComposedAnnotation(annotationClass, annotatedType.getAnnotations(), 1);
            if (composed != null) return composed;
        }
        return null;
    }

    /**
     * Recursively searches for {@code target} as a meta-annotation on the given
     * annotations, up to {@code maxDepth} levels deep.
     */
    private <T extends Annotation> T findComposedAnnotation(
        Class<T> target, Annotation[] annotations, int depth) {
        if (depth > 3) return null;
        for (Annotation a : annotations) {
            Class<? extends Annotation> aType = a.annotationType();
            // Skip standard Java meta-annotations to avoid infinite recursion
            if (aType.getName().startsWith("java.lang.annotation.")) continue;
            T found = aType.getAnnotation(target);
            if (found != null) return found;
            // Recurse into meta-annotations of this annotation
            T deeper = findComposedAnnotation(target, aType.getAnnotations(), depth + 1);
            if (deeper != null) return deeper;
        }
        return null;
    }

    /**
     * Use {@link StringAssembler} to find final type
     *
     * @param assembler is {@link StringAssembler} of input string
     * @return final class type
     */
    public Class<?> detectFinalClassWith(StringAssembler assembler) {
        if (!finalType) {
            Class<?> detectedType = this.type;
            if (!type.isPrimitive() && fixedObject != null) {
                detectedType = detectFinalType(assembler, rootType);
                checkFinalClass(detectedType);
            }
            this.afterTypeDetected();
            return detectedType;
        }
        return this.type;
    }

    /**
     * Use value object to get final type
     *
     * @param value object
     * @return final class type
     */
    public Class<?> detectFinalClassWith(Object value) {
        if (!finalType) {
            Class<?> detectedType = this.type;
            if (value != null) {
                detectedType = value.getClass();
            }
            this.afterTypeDetected();
            return detectedType;
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
