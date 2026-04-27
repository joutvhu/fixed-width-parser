package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;
import com.joutvhu.fixedwidth.parser.exception.CircularDependencyException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves field processing order based on {@link FixedConditional#dependsOnField()}
 * declarations, using a topological sort (Kahn's algorithm).
 *
 * <p>Circular dependencies are detected and reported as
 * {@link CircularDependencyException}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class FieldDependencyResolver {

    private FieldDependencyResolver() {}

    /**
     * Returns the fields in dependency-safe processing order.
     *
     * <p>Fields with no dependencies come first; fields that depend on others
     * come after their dependencies.  Fields that are not referenced by any
     * dependency declaration keep their original relative order.
     *
     * @param fields ordered list of {@link FixedTypeInfo} for the object's fields
     * @return the same fields in a safe processing order
     * @throws CircularDependencyException if a circular dependency is detected
     */
    public static List<FixedTypeInfo> sort(List<FixedTypeInfo> fields) {
        if (fields == null || fields.size() <= 1) return fields;

        // Build name → info map (preserving declaration order)
        Map<String, FixedTypeInfo> byName = new LinkedHashMap<>();
        for (FixedTypeInfo f : fields) {
            byName.put(f.getName(), f);
        }

        // Build adjacency: dependsOn → set of fields that depend on it
        Map<String, Set<String>> dependents = new HashMap<>();   // name → who depends on it
        Map<String, Set<String>> dependencies = new HashMap<>(); // name → what it depends on

        for (FixedTypeInfo f : fields) {
            String name = f.getName();
            dependencies.put(name, new HashSet<>());
            dependents.computeIfAbsent(name, k -> new HashSet<>());
        }

        for (FixedTypeInfo f : fields) {
            String name = f.getName();
            FixedConditional cond = f.getField() != null
                    ? f.getField().getAnnotation(FixedConditional.class) : null;
            if (cond != null) {
                String dep = cond.dependsOnField();
                if (byName.containsKey(dep)) {
                    dependencies.get(name).add(dep);
                    dependents.computeIfAbsent(dep, k -> new HashSet<>()).add(name);
                }
            }
        }

        // Kahn's algorithm — start with nodes that have no dependencies
        // Use a list to preserve original order among nodes with equal in-degree
        List<String> queue = new ArrayList<>();
        for (FixedTypeInfo f : fields) {
            if (dependencies.get(f.getName()).isEmpty()) {
                queue.add(f.getName());
            }
        }

        List<FixedTypeInfo> sorted = new ArrayList<>();
        Map<String, Set<String>> inDegree = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : dependencies.entrySet()) {
            inDegree.put(e.getKey(), new HashSet<>(e.getValue()));
        }

        while (!queue.isEmpty()) {
            String current = queue.remove(0);
            FixedTypeInfo info = byName.get(current);
            if (info != null) sorted.add(info);

            // For each field that depends on current, reduce its in-degree
            Set<String> deps = dependents.getOrDefault(current, Collections.emptySet());
            for (String dependent : deps) {
                Set<String> remaining = inDegree.get(dependent);
                if (remaining != null) {
                    remaining.remove(current);
                    if (remaining.isEmpty()) {
                        queue.add(dependent);
                    }
                }
            }
        }

        // If not all fields were processed, there's a cycle
        if (sorted.size() != fields.size()) {
            List<String> cycleFields = new ArrayList<>();
            for (FixedTypeInfo f : fields) {
                if (!sorted.contains(f)) cycleFields.add(f.getName());
            }
            throw new CircularDependencyException(
                    "Circular field dependency detected among: " + cycleFields);
        }

        return sorted;
    }
}
