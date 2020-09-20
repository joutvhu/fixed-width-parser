package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.domain.Alignment;

/**
 * Get or set a string value at a specified position.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public interface StringAssembler {
    /**
     * Get value string
     *
     * @return string
     */
    String getValue();

    /**
     * Get length of the value
     *
     * @return length
     */
    int length();

    /**
     * Get sub-string by start position and length
     *
     * @param start  start position
     * @param length is size
     * @return sub-string
     */
    String get(Integer start, Integer length);

    /**
     * Get sub-string by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return sub-string
     */
    String get(FixedTypeInfo info);

    /**
     * Set sub-string at start position and length
     *
     * @param start  start position
     * @param length is size
     * @param value  sub-string
     * @return this
     */
    StringAssembler set(Integer start, Integer length, String value);

    /**
     * Set sub-string by {@link FixedTypeInfo}
     *
     * @param info  the {@link FixedTypeInfo}
     * @param value sub-string
     * @return this
     */
    StringAssembler set(FixedTypeInfo info, String value);

    /**
     * Padding value by {@link FixedTypeInfo} and default alignment
     *
     * @param info             the {@link FixedTypeInfo}
     * @param defaultAlignment default alignment
     * @return this
     */
    StringAssembler pad(FixedTypeInfo info, Alignment defaultAlignment);

    /**
     * Padding value by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return this
     */
    StringAssembler pad(FixedTypeInfo info);

    /**
     * Trim value by {@link FixedTypeInfo} and default alignment
     *
     * @param info             the {@link FixedTypeInfo}
     * @param defaultAlignment default alignment
     * @return this
     */
    StringAssembler trim(FixedTypeInfo info, Alignment defaultAlignment);

    /**
     * Trim value by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return this
     */
    StringAssembler trim(FixedTypeInfo info);

    /**
     * Get child {@link StringAssembler} by start position and length
     *
     * @param start  start position
     * @param length is size
     * @return child {@link StringAssembler}
     */
    StringAssembler child(Integer start, Integer length);

    /**
     * Get child {@link StringAssembler} by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return child {@link StringAssembler}
     */
    StringAssembler child(FixedTypeInfo info);

    /**
     * Check is blank value by blank padding character.
     *
     * @param blankPadding blank padding character.
     * @return is blank
     */
    boolean isBlank(Character blankPadding);

    /**
     * Check is blank value by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return is blank
     */
    boolean isBlank(FixedTypeInfo info);
}
