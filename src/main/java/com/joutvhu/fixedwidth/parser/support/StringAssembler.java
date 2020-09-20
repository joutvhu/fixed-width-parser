package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.domain.Alignment;

/**
 * Get or set a string value at a specified position.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface StringAssembler {
    String getValue();

    int length();

    String get(Integer start, Integer length);

    String get(FixedTypeInfo info);

    StringAssembler set(Integer start, Integer length, String value);

    StringAssembler set(FixedTypeInfo info, String value);

    StringAssembler pad(FixedTypeInfo info, Alignment defaultAlignment);

    StringAssembler pad(FixedTypeInfo info);

    StringAssembler trim(FixedTypeInfo info, Alignment defaultAlignment);

    StringAssembler trim(FixedTypeInfo info);

    StringAssembler child(Integer start, Integer length);

    StringAssembler child(FixedTypeInfo info);

    boolean isBlank(Character blankPadding);

    boolean isBlank(FixedTypeInfo info);
}
