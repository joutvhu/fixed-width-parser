package com.joutvhu.fixedwidth.parser.codegen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Discovers and caches generated {@code $FixedWidth} companion instances via
 * {@link ServiceLoader}.
 *
 * <p>Each companion class implements both {@link FixedFieldAccessor} and
 * {@link FixedMetaProvider}, so a single SPI entry (registered under
 * {@code FixedFieldAccessor}) is enough to serve both roles.
 *
 * <p>The registry is populated once at class-initialisation time and is
 * immutable afterwards — safe for concurrent access without synchronisation.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public final class FixedCompanionRegistry {

    /** Keyed by model class; value implements both FixedFieldAccessor and FixedMetaProvider. */
    private static final Map<Class<?>, FixedFieldAccessor<?>> REGISTRY;

    static {
        Map<Class<?>, FixedFieldAccessor<?>> map = new HashMap<>();
        try {
            // Companions are registered under FixedFieldAccessor in the SPI file.
            // Because they also implement FixedMetaProvider, we can cast freely.
            ServiceLoader.load(FixedFieldAccessor.class)
                .forEach(c -> map.put(c.targetClass(), c));
        } catch (Exception e) {
            // ServiceLoader failure must not break the library —
            // callers fall back to Reflection transparently.
        }
        REGISTRY = Collections.unmodifiableMap(map);
    }

    private FixedCompanionRegistry() {
    }

    /**
     * Returns the generated {@link FixedFieldAccessor} for the given model class,
     * or {@code null} if no companion was generated.
     */
    @SuppressWarnings("unchecked")
    public static <T> FixedFieldAccessor<T> getAccessor(Class<T> type) {
        return (FixedFieldAccessor<T>) REGISTRY.get(type);
    }

    /**
     * Returns the generated {@link FixedMetaProvider} for the given model class,
     * or {@code null} if no companion was generated.
     *
     * <p>The companion implements both interfaces, so this is a safe cast.
     */
    @SuppressWarnings("unchecked")
    public static <T> FixedMetaProvider<T> getMetaProvider(Class<T> type) {
        FixedFieldAccessor<?> companion = REGISTRY.get(type);
        if (companion instanceof FixedMetaProvider) {
            return (FixedMetaProvider<T>) companion;
        }
        return null;
    }
}
