package com.joutvhu.fixedwidth.parser.codegen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Discovers and caches generated {@link FixedMetaProvider} instances
 * via {@link ServiceLoader}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class MetaProviderRegistry {

    private static final Map<Class<?>, FixedMetaProvider<?>> REGISTRY;

    static {
        Map<Class<?>, FixedMetaProvider<?>> map = new HashMap<>();
        try {
            ServiceLoader.load(FixedMetaProvider.class)
                .forEach(p -> map.put(p.targetClass(), p));
        } catch (Exception e) {
            // Ignored, fallback to reflection
        }
        REGISTRY = Collections.unmodifiableMap(map);
    }

    private MetaProviderRegistry() {
    }

    @SuppressWarnings("unchecked")
    public static <T> FixedMetaProvider<T> get(Class<T> type) {
        return (FixedMetaProvider<T>) REGISTRY.get(type);
    }
}
