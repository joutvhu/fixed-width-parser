package com.joutvhu.fixedwidth.parser.util;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
@UtilityClass
public class FixedHelper {
    private Map<Class<?>, Class<?>> CLASS_MAP = CommonUtil.mapOfEntries(
            CommonUtil.mapEntryOf(Collection.class, ArrayList.class),
            CommonUtil.mapEntryOf(AbstractCollection.class, ArrayList.class),
            CommonUtil.mapEntryOf(AbstractList.class, ArrayList.class),
            CommonUtil.mapEntryOf(List.class, ArrayList.class),
            CommonUtil.mapEntryOf(Set.class, HashSet.class),
            CommonUtil.mapEntryOf(AbstractSet.class, HashSet.class),
            CommonUtil.mapEntryOf(SortedSet.class, TreeSet.class),
            CommonUtil.mapEntryOf(NavigableSet.class, TreeSet.class),
            CommonUtil.mapEntryOf(Queue.class, LinkedList.class),
            CommonUtil.mapEntryOf(Deque.class, LinkedList.class),
            CommonUtil.mapEntryOf(AbstractSequentialList.class, LinkedList.class),
            CommonUtil.mapEntryOf(Map.class, HashMap.class),
            CommonUtil.mapEntryOf(AbstractMap.class, HashMap.class),
            CommonUtil.mapEntryOf(SortedMap.class, TreeMap.class),
            CommonUtil.mapEntryOf(NavigableMap.class, TreeMap.class),
            CommonUtil.mapEntryOf(Dictionary.class, Hashtable.class),
            CommonUtil.mapEntryOf(Temporal.class, Instant.class)
    );

    /**
     * New instance by object type.
     *
     * @param type class
     * @param <T>  is type of the object
     * @return new instance object
     */
    public <T> T newInstanceOf(Class<T> type) {
        try {
            Constructor<?> constructor = type.getConstructor();
            if (constructor != null) return (T) constructor.newInstance();
            else throw new UnsupportedOperationException(String
                    .format("%s class do not have a no-arg constructor.", type.getName()));
        } catch (NoSuchMethodException | SecurityException | InstantiationException |
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new FixedException(e);
        }
    }

    /**
     * Select a perfect type of a given type
     *
     * @param type supper-class type
     * @return sub-class type
     */
    public Class<?> selectSubTypeOf(Class<?> type) {
        int modifiers = type.getModifiers();
        if (!Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers))
            return type;

        if (CLASS_MAP.containsKey(type))
            return CLASS_MAP.get(type);

        List<?> sub = getNormalTypesOf(type, "java.util.", "java.", "javax.");
        if (CommonUtil.isNotBlank(sub))
            return (Class<?>) sub.get(0);
        return null;
    }

    /**
     * Gets all sub-types in hierarchy of a given type
     *
     * @param type        supper-class type
     * @param firstPrefix is prefixes are ranked first on the list
     * @param <T>         supper type
     * @return list of sub-types
     */
    public <T> List<Class<?>> getNormalTypesOf(Class<T> type, String... firstPrefix) {
        return new Reflections()
                .getSubTypesOf(type)
                .stream()
                .filter(c -> {
                    Constructor<?> constructor = IgnoreError.execute(() -> c.getConstructor());
                    if (constructor == null) return false;
                    int mod = c.getModifiers();
                    return Modifier.isPublic(mod) && !Modifier.isInterface(mod) && !Modifier.isAbstract(mod);
                })
                .sorted((o1, o2) -> {
                    String n1 = o1.getName();
                    String n2 = o2.getName();
                    for (String p : firstPrefix) {
                        boolean j1 = n1.startsWith(p);
                        boolean j2 = n2.startsWith(p);
                        if (j1 && !j2) return -1;
                        if (!j1 && j2) return 1;
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }
}
