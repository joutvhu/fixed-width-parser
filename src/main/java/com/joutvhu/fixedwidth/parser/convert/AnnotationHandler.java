package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * Contract for annotation-driven handlers in the parse/export pipeline.
 *
 * <p>Implementations are discovered automatically when their annotation is
 * annotated with {@link com.joutvhu.fixedwidth.parser.annotation.FixedHandler}.
 *
 * <p>A handler declares:
 * <ul>
 *   <li>Which {@link Phase}s it wants to be called at via {@link #getPhases}</li>
 *   <li>Which field names it depends on (for dependency-ordered parsing) via
 *       {@link #getDependencies}</li>
 *   <li>The actual logic in {@link #handle}</li>
 * </ul>
 *
 * @param <A> the annotation type this handler processes
 * @author Giao Ho
 * @since 2.0.0
 */
public interface AnnotationHandler<A extends Annotation> {

    /**
     * The phases at which this handler wants to be invoked.
     * Default: {@link Phase#READ_AFTER_TRANSFORM}.
     */
    default Set<Phase> getPhases(A annotation) {
        return Collections.singleton(Phase.READ_AFTER_TRANSFORM);
    }

    /**
     * Field names that must be parsed before this handler's field.
     * Return an empty set (the default) when there are no ordering requirements.
     */
    default Set<String> getDependencies(A annotation, FixedTypeInfo info) {
        return Collections.emptySet();
    }

    /**
     * Executes the handler logic.
     * Use {@code ctx.getPhase()} to distinguish between phases when the
     * handler is registered for more than one phase.
     */
    void handle(A annotation, FixedTypeInfo info, ParseContext context);
}
