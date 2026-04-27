package com.joutvhu.fixedwidth.parser.module;

import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.convert.AnnotationHandler;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthValidator;
import com.joutvhu.fixedwidth.parser.convert.FixedWidthWriter;
import com.joutvhu.fixedwidth.parser.convert.ParsingApprover;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.WriteStrategy;
import com.joutvhu.fixedwidth.parser.util.IgnoreError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Fixed module — manages readers, writers, validators, and annotation handlers.
 *
 * <p>Phase 2 adds {@link #invokeAnnotationHandlers}: for every annotation on the
 * field/class that carries {@link FixedHandler}, the declared
 * {@link AnnotationHandler} is instantiated and invoked at the current phase.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class FixedModule {

    private Set<Class<? extends FixedWidthReader>> readers = new LinkedHashSet<>();
    private Set<Class<? extends FixedWidthWriter>> writers = new LinkedHashSet<>();
    private Set<Class<? extends FixedWidthValidator>> validators = new LinkedHashSet<>();

    public FixedModule(Class<?>... classes) {
        for (Class<?> c : classes) {
            if (FixedWidthReader.class.isAssignableFrom(c))
                this.readers.add((Class<? extends FixedWidthReader>) c);
            else if (FixedWidthWriter.class.isAssignableFrom(c))
                this.writers.add((Class<? extends FixedWidthWriter>) c);
            else if (FixedWidthValidator.class.isAssignableFrom(c))
                this.validators.add((Class<? extends FixedWidthValidator>) c);
        }
    }

    // ── Module composition ────────────────────────────────────────────────────

    public FixedModule merge(FixedModule module) {
        readers.addAll(module.readers);
        writers.addAll(module.writers);
        validators.addAll(module.validators);
        return this;
    }

    // ── Reader / Writer / Validator (existing mechanism) ─────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends ParsingApprover> List<T> createHandlersBy(
            boolean takeOne, Set<Class<? extends T>> handlers, FixedTypeInfo info,
            FixedParseStrategy strategy, Class<?> strategyType) {
        List<T> result = new ArrayList<>();
        for (Class<? extends T> handlerClass : handlers) {
            T handler = IgnoreError.execute(() -> {
                Constructor<? extends T> constructor;
                if (strategyType != null) {
                    constructor = handlerClass.getConstructor(FixedTypeInfo.class, strategyType);
                    return constructor.newInstance(info, strategy);
                } else {
                    constructor = handlerClass.getConstructor(FixedTypeInfo.class);
                    return constructor.newInstance(info);
                }
            });
            if (handler != null) {
                result.add(handler);
                if (takeOne) return result;
            }
        }
        return result;
    }

    private <T extends ParsingApprover> T createHandlerBy(
            Set<Class<? extends T>> handlers, FixedTypeInfo info,
            FixedParseStrategy strategy, Class<?> strategyType) {
        List<T> result = createHandlersBy(true, handlers, info, strategy, strategyType);
        return result.isEmpty() ? null : result.get(0);
    }

    public final FixedWidthReader<Object> createReaderBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(readers, info, strategy, ReadStrategy.class);
    }

    public final FixedWidthWriter<Object> createWriterBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlerBy(writers, info, strategy, WriteStrategy.class);
    }

    public final List<FixedWidthValidator> createValidatorsBy(FixedTypeInfo info, FixedParseStrategy strategy) {
        return createHandlersBy(false, validators, info, strategy, null);
    }

    // ── Annotation handler dispatch (Phase 2) ────────────────────────────────

    /**
     * Scans all annotations on the field/class represented by {@code info},
     * finds those annotated with {@link FixedHandler}, instantiates the declared
     * {@link AnnotationHandler}, and invokes it if it is registered for the
     * current phase.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void invokeAnnotationHandlers(FixedTypeInfo info, ParseContext ctx) {
        if (ctx == null) return;
        Phase currentPhase = ctx.getPhase();

        for (Annotation annotation : collectAnnotations(info)) {
            FixedHandler fixedHandler = annotation.annotationType()
                    .getAnnotation(FixedHandler.class);
            if (fixedHandler == null) continue;

            Class<? extends AnnotationHandler<?>> handlerClass = fixedHandler.value();
            AnnotationHandler handler = IgnoreError.execute(() -> {
                Constructor ctor = handlerClass.getConstructor();
                return (AnnotationHandler) ctor.newInstance();
            });
            if (handler == null) continue;

            Set<Phase> phases = handler.getPhases(annotation);
            if (phases != null && phases.contains(currentPhase)) {
                handler.handle(annotation, info, ctx);
            }
        }
    }

    /**
     * Collects all annotations relevant to the given {@link FixedTypeInfo}:
     * field annotations, annotated-type annotations, and class-level annotations.
     *
     * <p>Also unwraps composed annotations (meta-annotations) up to depth 3.
     * For example, if a field has {@code @StandardDate} and that annotation is
     * itself annotated with {@code @FixedFormat}, the {@code @FixedFormat}
     * instance is included in the result.
     */
    private List<Annotation> collectAnnotations(FixedTypeInfo info) {
        List<Annotation> result = new ArrayList<>();
        if (info.getField() != null) {
            for (Annotation a : info.getField().getAnnotations()) {
                result.add(a);
                collectComposedAnnotations(a, result, 1);
            }
        }
        if (info.getAnnotatedType() != null) {
            for (Annotation a : info.getAnnotatedType().getAnnotations()) {
                result.add(a);
                collectComposedAnnotations(a, result, 1);
            }
        }
        if (info.getType() != null) {
            for (Annotation a : info.getType().getAnnotations()) {
                result.add(a);
                collectComposedAnnotations(a, result, 1);
            }
        }
        return result;
    }

    /**
     * Recursively collects meta-annotations from {@code annotation} up to
     * {@code maxDepth} levels, skipping standard Java meta-annotations.
     */
    private void collectComposedAnnotations(Annotation annotation, List<Annotation> result, int depth) {
        if (depth > 3) return;
        for (Annotation meta : annotation.annotationType().getAnnotations()) {
            if (meta.annotationType().getName().startsWith("java.lang.annotation.")) continue;
            result.add(meta);
            collectComposedAnnotations(meta, result, depth + 1);
        }
    }
}
