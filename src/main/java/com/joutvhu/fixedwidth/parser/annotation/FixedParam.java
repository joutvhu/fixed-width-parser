package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.model.Alignment;
import com.joutvhu.fixedwidth.parser.model.Padding;

import java.lang.annotation.*;

@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedParam {
    int length();

    char padding() default Padding.AUTO;

    Alignment alignment() default Alignment.AUTO;
}
