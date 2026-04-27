package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.convert.handler.ConditionalHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes a field conditional: the field is only parsed/exported when another
 * field in the same object has a specific value.
 *
 * <p>The dependency resolver (Phase 4) uses {@code dependsOnField} to ensure
 * the referenced field is always parsed before this one.
 *
 * <p>Example:
 * <pre>{@code
 * @FixedField(start = 0, length = 1)
 * private String type;
 *
 * @FixedConditional(dependsOnField = "type", whenValue = "A")
 * @FixedField(start = 1, length = 10)
 * private String dataA;   // only parsed when type == "A"
 * }</pre>
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@FixedHandler(ConditionalHandler.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedConditional {

    /**
     * Name of the field whose value determines whether this field is active.
     */
    String dependsOnField();

    /**
     * The value that {@code dependsOnField} must equal for this field to be active.
     */
    String whenValue();
}
