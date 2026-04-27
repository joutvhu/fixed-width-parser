package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.KeepPadding;
import com.joutvhu.fixedwidth.parser.domain.Padding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures padding behaviour for a field, class, or generic type parameter.
 *
 * <p>When placed on a field this annotation takes precedence over the
 * equivalent attributes in {@link FixedField} (which are deprecated).
 * When placed on a class it acts as the default for all fields in that class;
 * a field-level {@code @FixedPadding} overrides the class-level one.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedPadding {

    /**
     * Padding character used when the value is shorter than the field length.
     */
    char value() default Padding.AUTO;

    /**
     * Padding character used when the value is {@code null}.
     */
    char nullValue() default Padding.AUTO;

    /**
     * Whether to retain or strip the padding character when parsing.
     */
    KeepPadding keep() default KeepPadding.AUTO;

    /**
     * Alignment direction for padding.
     */
    Alignment alignment() default Alignment.AUTO;
}
