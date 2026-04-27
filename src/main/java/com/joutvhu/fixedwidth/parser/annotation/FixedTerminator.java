package com.joutvhu.fixedwidth.parser.annotation;

import java.lang.annotation.*;

/**
 * Stops reading collection elements when a terminator character/string is
 * encountered in the input.
 *
 * <p>Example — {@code "AAABBBCCC|          "} with {@code @FixedTerminator("|")}
 * produces {@code ["AAA", "BBB", "CCC"]}.
 *
 * @author Giao Ho
 * @since 2.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FixedTerminator {

    /** The terminator string that signals the end of the collection. */
    String value();
}
