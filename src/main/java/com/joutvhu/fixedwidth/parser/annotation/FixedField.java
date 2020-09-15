package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.model.Alignment;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedField {
    /**
     * Field label
     */
    String label();

    /**
     * Start position of value
     */
    int start() default 0;

    /**
     * Length of value
     */
    int length();

    /**
     * Value can't blank.
     */
    boolean require() default false;

    char padding() default ' ';

    Alignment alignment() default Alignment.AUTO;
}
