package com.joutvhu.fixedwidth.parser.annotation;

import com.joutvhu.fixedwidth.parser.convert.handler.EncodingHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the character encoding used to read or write a fixed-width field.
 *
 * <p>When present, the raw bytes of the field are re-interpreted using the
 * declared charset.  This is useful when a fixed-width file mixes encodings
 * (e.g. most fields are UTF-8 but a legacy field is ISO-8859-1).
 *
 * <p>Example:
 * <pre>{@code
 * @FixedField(start = 0, length = 10)
 * @FixedEncoding("ISO-8859-1")
 * private String legacyName;
 * }</pre>
 *
 * @author Giao Ho
 * @since 1.7.0
 */
@FixedHandler(EncodingHandler.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface FixedEncoding {

    /**
     * The charset name to use for this field (e.g. {@code "UTF-8"}, {@code "ISO-8859-1"}).
     * Must be a valid {@link java.nio.charset.Charset} name.
     */
    String value();
}
