package com.joutvhu.fixedwidth.parser.convert;

import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ParseContext;
import com.joutvhu.fixedwidth.parser.support.Phase;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Unified interface for all hooks in the fixed-width pipeline.
 *
 * <p>Completely replaces {@code FixedWidthReader}, {@code FixedWidthWriter},
 * and {@code AnnotationHandler}. A single Hook handles both parse and export
 * by checking {@code ctx.getPhase()} inside {@link #handle}.
 *
 * <p><b>Lifecycle:</b>
 * <ul>
 *   <li>Annotation-hook: newly initialized for each {@link #handle} call (per-invocation).</li>
 *   <li>Module-hook: singleton — initialized once when registered with the module.</li>
 * </ul>
 *
 * <p><b>Thread-safety:</b> Module-hooks must be thread-safe because instances are reused
 * across multiple concurrent parse/export calls. Annotation-hooks do not need to be thread-safe.
 *
 * @author Giao Ho
 * @since 3.0.0
 */
public interface Hook {

    /**
     * Executes the hook logic at the current phase.
     */
    void handle(FixedTypeInfo info, ParseContext ctx);

    /**
     * The set of phases that this hook wants to be called for.
     * Default: all phases.
     */
    default Set<Phase> phases() {
        return EnumSet.allOf(Phase.class);
    }

    /**
     * Names of fields that must be processed before this field.
     * Default: empty set.
     */
    default Set<String> dependencies(FixedTypeInfo info) {
        return Collections.emptySet();
    }
}
