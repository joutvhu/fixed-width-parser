package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.*;

/**
 * Controls how many elements are read from / written to a collection field.
 *
 * <p>Two modes:
 * <ul>
 *   <li>{@link #value()} — fixed count known at compile time</li>
 *   <li>{@link #field()} — count read from another field in the same object</li>
 * </ul>
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedCount {

    /**
     * Fixed number of elements.  Use {@code -1} (the default) to indicate that
     * the count should be read from {@link #field()} instead.
     */
    int value() default -1;

    /**
     * Name of the field in the same object that holds the element count.
     * Ignored when {@link #value()} is {@code >= 0}.
     */
    String field() default "";
}
