package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate the generic type parameters of collection and map fields.
 *
 * <p>For padding, alignment, and keep-padding configuration use
 * {@link FixedPadding} as a type-use annotation on the same parameter.
 *
 * <p>Example:
 * <pre>{@code
 * List<@FixedParam(length = 5) @FixedPadding(value = '0', alignment = Alignment.RIGHT) String> items
 * }</pre>
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedParam {
    /**
     * Setups the label name
     *
     * @return label name of the param
     */
    String label() default "";

    /**
     * Setups the length of the param.
     * 0 is unlimited length.
     *
     * @return length for the param
     */
    int length();
}
