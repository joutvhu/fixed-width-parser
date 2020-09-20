package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.*;

/**
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
     * Setups the length of the type
     * 0 is unlimited length
     *
     * @return length for the type
     */
    int length() default 0;

    /**
     * List subtype
     *
     * @return length for the type
     */
    Type[] subTypes() default {};

    /**
     * Default type if can't be choices any subtype
     *
     * @return default type
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
         * Property name use to check condition
         * If prop is blank then length must be grater than 0
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
         * Selection condition: value must be equals with one of the options
         *
         * @return value options
         */
        String[] oneOf() default {};

        /**
         * Selection condition: value must be matching with the regular expression
         *
         * @return a regex
         */
        String matchWith() default "";
    }
}
