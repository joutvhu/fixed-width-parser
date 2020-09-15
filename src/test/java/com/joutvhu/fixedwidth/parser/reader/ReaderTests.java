package com.joutvhu.fixedwidth.parser.reader;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class ReaderTests {
    @Test
    public void read() {
        Map<String, Integer> s = new HashMap<>();
        Class<?> v = s.getClass();
        Type[] c = v.getGenericInterfaces();
        Type l = v.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) v.getGenericSuperclass();
        TypeVariable<?> typeVariable = (TypeVariable<?>) parameterizedType.getActualTypeArguments()[0];
        Class<?>[] d = v.getClasses();
        Class<?> v2 = v.getComponentType();
        Type x = null;
    }
}
