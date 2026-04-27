package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Splits a collection field by a delimiter character/string instead of using
 * fixed-length elements.
 *
 * <p>Example — {@code "AA,BBB,CC,DDDD      "} with {@code @FixedDelimiter(",")}
 * produces {@code ["AA", "BBB", "CC", "DDDD"]}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedDelimiter {

    /**
     * The delimiter string used to split elements.
     */
    String value();

    /**
     * Whether to trim each element after splitting. Default {@code true}.
     */
    boolean trim() default true;
}
