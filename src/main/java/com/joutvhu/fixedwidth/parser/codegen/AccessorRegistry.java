package com.joutvhu.fixedwidth.parser.codegen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Discovers and caches generated {@link FixedFieldAccessor} instances
 * via {@link ServiceLoader}. The registry is populated once at class
 * initialisation time and is immutable afterwards.
 *
 * <p>If no accessor is found for a given class, the caller should
 * fall back to the existing Reflection-based field access.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class AccessorRegistry {

    private static final Map<Class<?>, FixedFieldAccessor<?>> REGISTRY;

    static {
        Map<Class<?>, FixedFieldAccessor<?>> map = new HashMap<>();
        try {
            ServiceLoader.load(FixedFieldAccessor.class)
                .forEach(a -> map.put(a.targetClass(), a));
        } catch (Exception e) {
            // ServiceLoader failure should not break the library —
            // the Reflection fallback will be used instead.
        }
        REGISTRY = Collections.unmodifiableMap(map);
    }

    private AccessorRegistry() {
    }

    /**
     * Returns the generated accessor for the given model class,
     * or {@code null} if none was generated.
     */
    @SuppressWarnings("unchecked")
    public static <T> FixedFieldAccessor<T> get(Class<T> type) {
        return (FixedFieldAccessor<T>) REGISTRY.get(type);
    }
}
