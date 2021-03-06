package com.joutvhu.fixedwidth.parser.constraint;

import java.lang.annotation.*;

/**
 * Setups constraint for value by options
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedOption {
    /**
     * The fixed width value must be equals with one of the options
     *
     * @return the options
     */
    String[] options();

    /**
     * Return {@code true} if you want the value must be contains in the options,
     * otherwise {@code false} is returned.
     *
     * @return contains or not contains
     */
    boolean contains() default true;

    /**
     * Customize error message
     * If message is blank the system with be generate a message.
     *
     * @return error message
     */
    String message() default "";

    /**
     * The message is native
     *
     * @return is native message
     */
    boolean nativeMessage() default false;
}
