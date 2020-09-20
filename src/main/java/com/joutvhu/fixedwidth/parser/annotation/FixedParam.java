package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.domain.Padding;

import java.lang.annotation.*;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedParam {
    /**
     * Setups the label name
     *
     * @return label name of the param
     */
    String label() default "";

    /**
     * Setups the length of the param
     * 0 is unlimited length
     *
     * @return length for the param
     */
    int length();

    /**
     * Sets the padding character of the fixed width param
     *
     * @return padding of the fixed width param
     */
    char padding() default Padding.AUTO;

    /**
     * Sets the padding character for null value
     *
     * @return padding of the null value
     */
    char nullPadding() default Padding.AUTO;

    /**
     * Configures whether to retain the padding character when parsing values for this param
     *
     * @return flag indicating the padding character should be kept in the parsed value
     */
    KeepPadding keepPadding() default KeepPadding.AUTO;

    /**
     * Sets the alignment of the fixed width param
     *
     * @return alignment of the fixed width param
     */
    Alignment alignment() default Alignment.AUTO;
}
