package com.joutvhu.fixedwidth.parser.constraint;

import java.lang.annotation.*;

/**
 * Setups constraint for value by regular expression
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedRegex {
    /**
     * The fixed width value must be matching the regex
     *
     * @return regular expression
     */
    String regex();

    /**
     * Regex flags
     *
     * @return {@link com.google.re2j.Pattern} flags
     */
    int flags() default 0;

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
