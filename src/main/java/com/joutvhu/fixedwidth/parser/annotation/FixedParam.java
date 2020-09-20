package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.Padding;

import java.lang.annotation.*;

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
     * Sets the padding character of the fixed width field
     *
     * @return padding of the fixed width field
     */
    char padding() default Padding.AUTO;

    /**
     * Sets the alignment of the fixed width field
     *
     * @return alignment of the fixed width field
     */
    Alignment alignment() default Alignment.AUTO;
}
