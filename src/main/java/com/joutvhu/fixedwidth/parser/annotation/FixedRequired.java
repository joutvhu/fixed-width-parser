package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as required: exporting a {@code null} value throws
 * {@link com.joutvhu.fixedwidth.parser.exception.MandatoryValueException}.
 *
 * <p>This annotation replaces the deprecated {@code required} attribute of
 * {@link FixedField}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedRequired {

    /**
     * Optional custom error message.
     */
    String message() default "";
}
