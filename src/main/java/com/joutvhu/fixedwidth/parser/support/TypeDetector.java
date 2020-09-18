package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;
import lombok.Getter;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

@Getter
public abstract class TypeDetector {
    protected Field field;
    protected Class<?> type;
    protected AnnotatedType annotatedType;

    protected FixedField fixedField;
    protected FixedParam fixedParam;
    protected FixedObject fixedObject;

    private boolean finalType = false;
    private SourceType sourceType;

    public TypeDetector(Class<?> type) {
        Assert.notNull(type, "Class Type must not be null!");
        this.type = type;

        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.notNull(fixedObject, String.format("The %s class must be annotated with FixedObject.", type.getName()));
        Assert.isTrue(fixedObject.length() >= 0, "Length of object can't less than 0.");

        this.sourceType = SourceType.CLASS_TYPE;
    }

    public TypeDetector(AnnotatedType annotatedType) {
        Assert.notNull(annotatedType, "AnnotatedType must not be null!");
        Type t = annotatedType.getType();
        Assert.isTrue(t instanceof Class, String.format("The %s type is not a class.", t.getTypeName()));
        this.type = (Class<?>) t;
        this.annotatedType = annotatedType;

        this.fixedParam = getAnnotation(FixedParam.class);
        Assert.notNull(fixedParam, String.format("The %s type must be annotated with FixedParam.", t.getTypeName()));
        Assert.isTrue(fixedParam.length() >= 0, "Length of param can't less than 0.");
        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.isTrue(fixedObject == null || fixedObject.length() >= 0, "Length of object can't less than 0.");

        this.sourceType = SourceType.PARAM_TYPE;
    }

    public TypeDetector(Field field) {
        Assert.notNull(field, "Field must not be null!");
        this.field = field;
        this.type = field.getType();

        this.fixedField = getAnnotation(FixedField.class);
        Assert.notNull(fixedField, String.format("The %s field must be annotated with FixedField.", field.getName()));
        this.fixedObject = getAnnotation(FixedObject.class);

        this.sourceType = SourceType.FIELD_TYPE;
    }

    public TypeDetector(Object value) {
        Assert.notNull(value, "Object must not be null!");
        this.type = value.getClass();

        this.fixedObject = getAnnotation(FixedObject.class);
        Assert.notNull(fixedObject, String.format("The %s class must be annotated with FixedObject.", type.getName()));

        this.finalType = true;
        this.sourceType = SourceType.OBJECT_TYPE;
    }

    @PostConstruct
    private void postConstruct() {
        this.afterInit();
        if (finalType)
            this.afterTypeDetected();
    }

    public void afterInit() {
        // This is virtual method.
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return ReflectionUtil.getAnnotation(annotationClass, field, annotatedType, type);
    }

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

    public void detectedNewType(Class<?> newType) {
        // This is virtual method.
    }

    public abstract void afterTypeDetected();

    enum SourceType {
        CLASS_TYPE,
        FIELD_TYPE,
        PARAM_TYPE,
        OBJECT_TYPE
    }
}
