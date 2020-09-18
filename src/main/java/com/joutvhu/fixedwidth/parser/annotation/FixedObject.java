package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedObject {
    /**
     * Object label
     */
    String label() default "";

    /**
     * Length of object
     */
    int length() default 0;

    Type[] subTypes() default {};

    Class<?> defaultSubType() default void.class;

    /**
     * Specify a sub-type of FixedObject and its select condition
     */
    @interface Type {
        Class<?> value();

        String prop() default "";

        int start() default 0;

        int length() default 0;

        String[] oneOf() default {};

        String matchWith() default "";
    }
}
