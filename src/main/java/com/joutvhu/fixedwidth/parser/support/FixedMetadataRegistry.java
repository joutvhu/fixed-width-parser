package com.joutvhu.fixedwidth.parser.support;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for caching FixedTypeInfo metadata.
 * Ensure thread safety and avoid redundant reflection.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FixedMetadataRegistry {
    private static final Map<Object, FixedTypeInfo> CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<Set<Object>> DETECTING = ThreadLocal.withInitial(HashSet::new);

    private FixedMetadataRegistry() {
    }

    public static FixedTypeInfo get(Class<?> type) {
        if (type == null) return null;
        FixedTypeInfo cached = CACHE.get(type);
        if (cached != null) return cached;

        if (DETECTING.get().contains(type)) {
            // Circular reference detected — return null to break the cycle.
            // detectFields() filters nulls so this field will be skipped.
            return null;
        }

        try {
            DETECTING.get().add(type);
            FixedTypeInfo info = FixedTypeInfo.of(type);
            CACHE.put(type, info);
            return info;
        } finally {
            DETECTING.get().remove(type);
        }
    }

    public static FixedTypeInfo get(java.lang.reflect.Field field) {
        if (field == null) return null;
        FixedTypeInfo cached = CACHE.get(field);
        if (cached != null) return cached;

        if (DETECTING.get().contains(field)) {
            return null;
        }

        try {
            DETECTING.get().add(field);
            FixedTypeInfo info = FixedTypeInfo.of(field);
            CACHE.put(field, info);
            return info;
        } finally {
            DETECTING.get().remove(field);
        }
    }

    public static FixedTypeInfo get(java.lang.reflect.AnnotatedType annotatedType) {
        if (annotatedType == null) return null;
        FixedTypeInfo cached = CACHE.get(annotatedType);
        if (cached != null) return cached;

        if (DETECTING.get().contains(annotatedType)) {
            return null;
        }

        try {
            DETECTING.get().add(annotatedType);
            FixedTypeInfo info = FixedTypeInfo.of(annotatedType);
            CACHE.put(annotatedType, info);
            return info;
        } finally {
            DETECTING.get().remove(annotatedType);
        }
    }

    public static void clear() {
        CACHE.clear();
    }
}
