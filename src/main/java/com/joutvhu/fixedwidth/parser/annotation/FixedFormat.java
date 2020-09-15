package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedFormat {
    /**
     * Value format
     */
    String format();

    String message() default "";
}
