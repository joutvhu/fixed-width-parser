package com.joutvhu.fixedwidth.parser.constraint;

import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.RegexHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Setups constraint for value by regular expression
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@FixedHandler(RegexHandler.class)
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
