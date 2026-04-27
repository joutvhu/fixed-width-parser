package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.convert.Hook;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that links an annotation to its hook class.
 *
 * <p>Any annotation annotated with {@code @FixedHandler} is automatically
 * recognised by the parser. When the annotated annotation is found on a
 * field or class, the declared {@link Hook} is instantiated (per-invocation,
 * no-arg constructor) and invoked at the phases it registers via
 * {@link Hook#getSupportedPhases()}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface FixedHandler {
    /**
     * The hook class to instantiate when this annotation is encountered.
     * Must have a no-arg constructor. Instantiated fresh for each invocation.
     */
    Class<? extends Hook> value();
}
