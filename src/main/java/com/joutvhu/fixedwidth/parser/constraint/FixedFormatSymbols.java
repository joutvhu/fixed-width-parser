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
     *
     * @return the character used for decimal sign.
     */
    String decimalSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setGroupingSeparator(char)}
     *
     * @return the character used for thousands separator.
     */
    String groupingSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setPatternSeparator(char)}
     *
     * @return the character used to separate positive and negative subpatterns in a pattern.
     */
    String patternSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setPercent(char)}
     *
     * @return the character used for percent sign.
     */
    String percent() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setZeroDigit(char)}
     *
     * @return the character used for zero.
     */
    String zeroDigit() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setDigit(char)}
     *
     * @return the character used for a digit in a pattern.
     */
    String digit() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setMinusSign(char)}
     *
     * @return the character used to represent minus sign.
     */
    String minusSign() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setExponentSeparator(String)}
     *
     * @return the string used to separate the mantissa from the exponent.
     */
    String exponentialSeparator() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setPerMill(char)}
     *
     * @return the character used for per mille sign.
     */
    String perMill() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setInfinity(String)}
     *
     * @return the string used to represent infinity.
     */
    String infinity() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setNaN(String)}
     *
     * @return the string used to represent "not a number".
     */
    String naN() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setInternationalCurrencySymbol(String)}
     *
     * @return the currency code.
     */
    String internationalCurrencySymbol() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setCurrencySymbol(String)}
     *
     * @return the currency symbol.
     */
    String currencySymbol() default "";

    /**
     * {@link java.text.DecimalFormatSymbols#setMonetaryDecimalSeparator(char)}
     *
     * @return the monetary decimal separator.
     */
    String monetarySeparator() default "";
}
