package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.Padding;

import java.lang.annotation.*;

@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedParam {
    /**
     * Param label
     */
    String label() default "";

    int length();

    char padding() default Padding.AUTO;

    Alignment alignment() default Alignment.AUTO;
}
