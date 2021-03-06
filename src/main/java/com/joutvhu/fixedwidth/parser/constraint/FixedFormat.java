package com.joutvhu.fixedwidth.parser.constraint;

import java.lang.annotation.*;

/**
 * Setups constraint for value by format
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedFormat {
    /**
     * The fixed width value must be matching with the format
     *
     * @return fixed width format
     */
    String format();

    /**
     * For create {@link java.text.DecimalFormatSymbols} when parse number.
     *
     * @return {@link FixedFormatSymbols}
     */
    FixedFormatSymbols formatSymbols() default @FixedFormatSymbols;

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
