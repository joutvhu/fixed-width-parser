package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.convert.AnnotationHandler;

import java.lang.annotation.*;

/**
 * Meta-annotation that links an annotation to its handler class.
 *
 * <p>Any annotation annotated with {@code @FixedHandler} is automatically
 * recognised by the parser. When the annotated annotation is found on a
 * field or class, the declared {@link AnnotationHandler} is instantiated
 * and invoked at the phases it registers via
 * {@link AnnotationHandler#getPhases}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface FixedHandler {
    /**
     * The handler class to instantiate when this annotation is encountered.
     */
    Class<? extends AnnotationHandler<?>> value();
}
