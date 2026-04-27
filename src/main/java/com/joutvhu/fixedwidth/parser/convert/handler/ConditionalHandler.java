package com.joutvhu.fixedwidth.parser.convert.handler;

import com.joutvhu.fixedwidth.parser.annotation.FixedConditional;
import com.joutvhu.fixedwidth.parser.convert.Hook;
import com.joutvhu.fixedwidth.parser.support.ContextFrame;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Handles {@link FixedConditional}: skips the current field when the referenced
 * dependency field does not equal {@link FixedConditional#whenValue()}.
 *
 * <p>During READ: runs at {@link Phase#READ_PRE_CUT}. Reads the dependency
 * field's value from the parent object's partial result and calls
 * {@link ParseContext#skipCurrentField()} when the condition is not met.
 *
 * <p>During WRITE: runs at {@link Phase#WRITE_PRE_GET}. Reads the dependency
 * field's value from the parent object and calls
 * {@link ParseContext#skipCurrentField()} when the condition is not met.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
public class ConditionalHandler implements Hook {

    @Override
    public Set<Phase> getSupportedPhases() {
        return new HashSet<>(Arrays.asList(Phase.READ_PRE_CUT, Phase.WRITE_PRE_GET));
    }

    @Override
    public Set<String> getDependencies(FixedTypeInfo info) {
        FixedConditional annotation = info.getAnnotation(FixedConditional.class);
        if (annotation == null) return Collections.emptySet();
        return Collections.singleton(annotation.dependsOnField());
    }

    @Override
    public void handle(FixedTypeInfo info, ParseContext ctx) {
        FixedConditional annotation = info.getAnnotation(FixedConditional.class);
        if (annotation == null) return;

        String dependsOn = annotation.dependsOnField();
        String whenValue = annotation.whenValue();

        // Get the parent object (partially built during READ, fully built during WRITE)
        ContextFrame parentFrame = ctx.parentFrame();
        if (parentFrame == null) return;

        Object parentObject = parentFrame.getPartialResult();
        if (parentObject == null) return;

        // Read the dependency field value via reflection
        String actualValue = getFieldValue(parentObject, dependsOn);

        // Skip this field if the condition is not met
        if (!Objects.equals(whenValue, actualValue)) {
            ctx.skipCurrentField();
        }
    }

    private String getFieldValue(Object obj, String fieldName) {
        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) {
            try {
                Field f = cls.getDeclaredField(fieldName);
                ReflectionUtil.makeAccessible(f);
                Object val = ReflectionUtil.getField(f, obj);
                return val != null ? val.toString() : null;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        return null;
    }
}
