package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate the fixed width object.
 *
 * <p>For padding, alignment, and keep-padding configuration use
 * {@link FixedPadding} on the class.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedObject {
    /**
     * Setups the label name
     *
     * @return label name of the type
     */
    String label() default "";

    /**
     * Setups the length of the type.
     * 0 is unlimited length.
     *
     * @return length for the type
     */
    int length() default 0;

    /**
     * List subtype
     *
     * @return subtypes for polymorphic detection
     */
    Type[] subTypes() default {};

    /**
     * Default type if no subtype matches
     *
     * @return default subtype class
     */
    Class<?> defaultSubType() default void.class;

    /**
     * Specify a subtype of FixedObject and its select condition
     */
    @interface Type {
        /**
         * Subclass type
         *
         * @return subclass type
         */
        Class<?> value();

        /**
         * Property name use to check condition.
         * If prop is blank then length must be greater than 0.
         *
         * @return property name
         */
        String prop() default "";

        /**
         * Setups the start position of the field
         *
         * @return start position for the field
         */
        int start() default 0;

        /**
         * Setups the length of the field
         *
         * @return length for the field
         */
        int length() default 0;

        /**
         * Selection condition: value must be equal to one of the options
         *
         * @return value options
         */
        String[] oneOf() default {};

        /**
         * Selection condition: value must match the regular expression
         *
         * @return a regex
         */
        String matchWith() default "";
    }
}
