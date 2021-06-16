package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.domain.Padding;

import java.lang.annotation.*;

/**
 * Used to annotate the fixed width field
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedField {
    /**
     * Setups the label name
     *
     * @return label name of the field
     */
    String label() default "";

    /**
     * Setups the start position of the field
     *
     * @return start position for the field
     */
    int start() default 0;

    /**
     * Setups the length of the field
     * 0 is unlimited length
     *
     * @return length for the field
     */
    int length();

    /**
     * The field is nullable
     *
     * @return is nullable
     */
    boolean require() default false;

    /**
     * Sets the padding character of the fixed width field
     *
     * @return padding of the fixed width field
     */
    char padding() default Padding.AUTO;

    /**
     * Sets the padding character for null value
     *
     * @return padding for the null value
     */
    char nullPadding() default Padding.AUTO;

    /**
     * Configures whether to retain the padding character when parsing values for this field
     *
     * @return flag indicating the padding character should be kept in the parsed value
     */
    KeepPadding keepPadding() default KeepPadding.AUTO;

    /**
     * Sets the alignment of the fixed width field
     *
     * @return alignment of the fixed width field
     */
    Alignment alignment() default Alignment.AUTO;
}
