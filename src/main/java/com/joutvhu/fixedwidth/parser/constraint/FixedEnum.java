package com.joutvhu.fixedwidth.parser.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure Enum mapping for fixed-width fields.
 *
 * @author Giao Ho
 * @since 1.2.1
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedEnum {
    /**
     * The property of the enum to use for mapping.
     * If blank, the enum name() will be used.
     *
     * @return the property name
     */
    String property() default "";

    /**
     * If true, perform case-insensitive matching when reading.
     *
     * @return true for case-insensitive matching
     */
    boolean ignoreCase() default true;
}
