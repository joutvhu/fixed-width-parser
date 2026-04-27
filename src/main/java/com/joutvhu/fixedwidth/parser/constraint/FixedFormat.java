package com.joutvhu.fixedwidth.parser.constraint;

import com.joutvhu.fixedwidth.parser.annotation.FixedHandler;
import com.joutvhu.fixedwidth.parser.convert.handler.FormatDispatchHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Setups constraint for value by format
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@FixedHandler(FormatDispatchHandler.class)
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
     * If message is blank the system with be generated a message.
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
