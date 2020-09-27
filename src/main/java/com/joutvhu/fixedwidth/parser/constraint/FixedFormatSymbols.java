package com.joutvhu.fixedwidth.parser.constraint;

import java.lang.annotation.*;

/**
 * For custom {@link java.text.DecimalFormatSymbols} properties when parse number.
 *
 * @author Giao Ho
 * @since 1.1.0
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedFormatSymbols {
    /**
     * {@link java.text.DecimalFormatSymbols#setDecimalSeparator(char)}
     */
    String decimalSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setGroupingSeparator(char)}
     */
    String groupingSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setPatternSeparator(char)}
     */
    String patternSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setPercent(char)}
     */
    String percent() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setZeroDigit(char)}
     */
    String zeroDigit() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setDigit(char)}
     */
    String digit() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setMinusSign(char)}
     */
    String minusSign() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setExponentSeparator(String)}
     */
    String exponentialSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setPerMill(char)}
     */
    String perMill() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setInfinity(String)}
     */
    String infinity() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setNaN(String)}
     */
    String naN() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setInternationalCurrencySymbol(String)}
     */
    String internationalCurrencySymbol() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setCurrencySymbol(String)}
     */
    String currencySymbol() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setMonetaryDecimalSeparator(char)}
     */
    String monetarySeparator() default "";
}
