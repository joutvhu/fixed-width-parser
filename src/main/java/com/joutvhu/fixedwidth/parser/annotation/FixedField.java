package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.*;

/**
 * Used to annotate the fixed width field.
 *
 * <p>For padding, alignment, and keep-padding configuration use
 * {@link FixedPadding}. For required-field validation use {@link FixedRequired}.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedField {
    /**
     * Setups the label name
     *
     * @return label name of the field
     */
    String label() default "";

    /**
     * Setups the start position of the field
     *
     * @return start position for the field
     */
    int start() default 0;

    /**
     * Setups the length of the field.
     * 0 is unlimited length.
     *
     * @return length for the field
     */
    int length();
}
