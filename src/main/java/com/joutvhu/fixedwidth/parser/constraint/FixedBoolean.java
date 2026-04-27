package com.joutvhu.fixedwidth.parser.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure true and false values for boolean fields
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedBoolean {
    /**
     * Values that will be parsed as true
     *
     * @return array of true string values
     */
    String[] trueValues() default {"Y", "YES", "T", "TRUE", "ON", "1"};

    /**
     * Values that will be parsed as false
     *
     * @return array of false string values
     */
    String[] falseValues() default {"N", "NO", "F", "FALSE", "OFF", "0"};
}
