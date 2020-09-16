package com.joutvhu.fixedwidth.parser.util;

import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class ClassUtil {
    private Map<Class<?>, Class<?>> CLASS_MAP = CommonUtil.mapOfEntries(
            CommonUtil.mapEntryOf(Collection.class, ArrayList.class),
            CommonUtil.mapEntryOf(AbstractCollection.class, ArrayList.class),
            CommonUtil.mapEntryOf(AbstractList.class, ArrayList.class),
            CommonUtil.mapEntryOf(List.class, ArrayList.class),
            CommonUtil.mapEntryOf(Set.class, HashSet.class),
            CommonUtil.mapEntryOf(AbstractSet.class, HashSet.class),
            CommonUtil.mapEntryOf(Queue.class, LinkedList.class),
            CommonUtil.mapEntryOf(Deque.class, LinkedList.class),
            CommonUtil.mapEntryOf(AbstractSequentialList.class, LinkedList.class),
            CommonUtil.mapEntryOf(Map.class, HashMap.class),
            CommonUtil.mapEntryOf(AbstractMap.class, HashMap.class)
    );

    public Class<?> selectSubTypeOf(Class<?> type) {
        int modifiers = type.getModifiers();
        if (!Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers))
            return type;

        if (CLASS_MAP.containsKey(type))
            return CLASS_MAP.get(type);

        List sub = getNormalTypesOf(type, "java.util.", "java.", "javax.");
        if (CommonUtil.isNotBlank(sub))
            return (Class<?>) sub.get(0);
        return null;
    }

    public <T> List<Class<?>> getNormalTypesOf(Class<T> type, String... firstPrefix) {
        return new Reflections()
                .getSubTypesOf(type)
                .stream()
                .filter(c -> {
                    Constructor constructor = IgnoreError.execute(() -> c.getConstructor());
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
