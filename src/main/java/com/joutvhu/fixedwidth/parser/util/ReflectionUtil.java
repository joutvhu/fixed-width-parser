package com.joutvhu.fixedwidth.parser.util;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
@UtilityClass
public class ReflectionUtil {
    public void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException)
            throw (RuntimeException) ex;
        if (ex instanceof Error)
            throw (Error) ex;
        throw new UndeclaredThrowableException(ex);
    }

    public void handleInvocationTargetException(InvocationTargetException ex) {
        rethrowRuntimeException(ex.getTargetException());
    }

    public void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException)
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        if (ex instanceof IllegalAccessException)
            throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
        if (ex instanceof InvocationTargetException)
            handleInvocationTargetException((InvocationTargetException) ex);
        if (ex instanceof RuntimeException)
            throw (RuntimeException) ex;
        throw new UndeclaredThrowableException(ex);
    }

    public Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    public void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        }
    }

    public void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible())
            field.setAccessible(true);
    }

    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) ||
                !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    public Type[] getActualTypeArguments(Field field) {
        try {
            ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
            return integerListType.getActualTypeArguments();
        } catch (Exception e) {
            return new Type[0];
        }
    }

    public AnnotatedType[] getAnnotatedActualTypeArguments(Field field) {
        try {
            AnnotatedParameterizedType annotatedType = (AnnotatedParameterizedType) field.getAnnotatedType();
            return annotatedType.getAnnotatedActualTypeArguments();
        } catch (Exception e) {
            return new AnnotatedType[0];
        }
    }

    public AnnotatedType[] getAnnotatedActualTypeArguments(AnnotatedParameterizedType annotatedType) {
        try {
            return annotatedType.getAnnotatedActualTypeArguments();
        } catch (Exception e) {
            return new AnnotatedType[0];
        }
    }

    /**
     * Find an annotation from some {@link AnnotatedElement}
     *
     * @param annotationClass   the Class object corresponding to the annotation type
     * @param annotatedElements the annotation sources {@link AnnotatedElement}
     * @param <T>               the type of the annotation to query for and return if present
     * @return annotation for the specified annotation type
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass, AnnotatedElement... annotatedElements) {
        for (AnnotatedElement annotatedElement : CommonUtil.defaultIfNull(annotatedElements, new AnnotatedElement[0])) {
            if (annotatedElement != null) {
                T annotation = annotatedElement.getAnnotation(annotationClass);
                if (annotation != null)
                    return annotation;
            }
        }
        return null;
    }

    public <T> T getValueFromAnnotation(String name, Class<T> type, Annotation annotation) {
        if (annotation == null || CommonUtil.isBlank(name))
            return null;
        return IgnoreError.execute(() -> {
            Method method = annotation.annotationType().getDeclaredMethod(name);
            makeAccessible(method);
            return type.cast(method.invoke(annotation));
        });
    }

    public <T> T getValueFromAnnotations(String name, Class<T> type, Annotation... annotations) {
        for (Annotation annotation : annotations) {
            if (annotation != null) {
                T result = getValueFromAnnotation(name, type, annotation);
                if (result != null) return result;
            }
        }
        return null;
    }

    public <T> T getDefaultValueOfAnnotation(String name, Class<T> type, Class<? extends Annotation> annotationType) {
        if (annotationType == null || CommonUtil.isBlank(name))
            return null;
        return IgnoreError.execute(() -> {
            Method method = annotationType.getDeclaredMethod(name);
            makeAccessible(method);
            return type.cast(method.getDefaultValue());
        });
    }

    public <T> T getDefaultValueOfAnnotations(String name, Class<T> type, Class<? extends Annotation>... annotationTypes) {
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            if (annotationType != null) {
                T result = getDefaultValueOfAnnotation(name, type, annotationType);
                if (result != null) return result;
            }
        }
        return null;
    }
}
