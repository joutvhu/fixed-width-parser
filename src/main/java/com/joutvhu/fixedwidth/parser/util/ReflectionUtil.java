package com.joutvhu.fixedwidth.parser.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.*;

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
}
