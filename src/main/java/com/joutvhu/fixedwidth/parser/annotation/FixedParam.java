package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.model.Alignment;

import java.lang.annotation.*;

@Target({ElementType.TYPE_PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedParam {
    int length();

    char padding() default ' ';

    Alignment alignment() default Alignment.AUTO;
}
