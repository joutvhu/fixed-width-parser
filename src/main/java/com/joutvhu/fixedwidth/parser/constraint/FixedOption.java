package com.joutvhu.fixedwidth.parser.constraint;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedOption {
    String[] options();

    String message() default "";

    boolean nativeMessage() default true;
}
