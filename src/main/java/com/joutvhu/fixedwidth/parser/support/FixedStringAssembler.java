package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Get or set a string value at a specified position.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
public class FixedStringAssembler implements StringAssembler {
    private String value;

    public FixedStringAssembler(String value) {
        Assert.notNull(value, "String value cannot be null.");
        this.value = value;
    }

    public static StringAssembler instance() {
        return new FixedStringAssembler(StringUtils.EMPTY);
    }

    public static StringAssembler black(FixedTypeInfo info) {
        String value = StringUtils.EMPTY;
        if (info.getLength() != null && info.getLength() > 0) {
            char blankPadding = CommonUtil.defaultIfNull(info.getDefaultNullPadding(), ' ');
            value = StringUtils.repeat(blankPadding, info.getLength());
        }
        return new FixedStringAssembler(value);
    }

    public static StringAssembler of(String value) {
        return new FixedStringAssembler(value);
    }

    private static String padString(String value, Integer start, Integer length) {
        int size = length != null ? start + length : start;
        if (value.length() < size)
            value = StringUtils.rightPad(value, size);
        return value;
    }

    private void replaceAt(Integer start, Integer length, String value) {
        if (length != null && length > 0)
            this.value = this.value.substring(0, start) +
                    CommonUtil.rightPadValue(value, length, ' ') +
                    this.value.substring(start + length);
        else
            this.value = this.value.substring(0, start) + value;
    }

    /**
     * Get value string
     *
     * @return string
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * Get length of the value
     *
     * @return length
     */
    @Override
    public int length() {
        return value != null ? value.length() : 0;
    }

    /**
     * Get sub-string by start position and length
     *
     * @param start  start position
     * @param length is size
     * @return sub-string
     */
    @Override
    public String get(Integer start, Integer length) {
        if (start == null) start = 0;
        if (length == 0) length = null;
        this.value = padString(this.value, start, length);
        return length != null ? value.substring(start, start + length) : value.substring(start);
    }

    /**
     * Get sub-string by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return sub-string
     */
    @Override
    public String get(FixedTypeInfo info) {
        return get(info.getStart(), info.getLength());
    }

    /**
     * Set sub-string at start position and length
     *
     * @param start  start position
     * @param length is size
     * @param value  sub-string
     * @return this
     */
    @Override
    public StringAssembler set(Integer start, Integer length, String value) {
        if (start == null) start = 0;
        if (value == null) value = StringUtils.EMPTY;
        this.value = padString(this.value, start, length);
        this.replaceAt(start, length, value);
        return this;
    }

    /**
     * Set sub-string by {@link FixedTypeInfo}
     *
     * @param info  the {@link FixedTypeInfo}
     * @param value sub-string
     * @return this
     */
    @Override
    public StringAssembler set(FixedTypeInfo info, String value) {
        return set(info.getStart(), info.getLength(), value);
    }

    private Alignment getAlignment(FixedTypeInfo info, Alignment defaultAlignment) {
        Alignment alignment = info.getAlignment();
        if (alignment == null || alignment == Alignment.AUTO) alignment = defaultAlignment;
        if (alignment == null || alignment == Alignment.AUTO) alignment = info.getDefaultAlignment();
        return alignment;
    }

    /**
     * Padding value by {@link FixedTypeInfo} and default alignment
     *
     * @param info             the {@link FixedTypeInfo}
     * @param defaultAlignment default alignment
     * @return this
     */
    @Override
    public StringAssembler pad(FixedTypeInfo info, Alignment defaultAlignment) {
        Integer length = info.getLength();
        if (length != null && length > 0) {
            Alignment alignment = getAlignment(info, defaultAlignment);
            Character padding = info.getDefaultPadding();

            switch (alignment) {
                case LEFT:
                    value = CommonUtil.rightPadValue(value, length, padding);
                    break;
                case RIGHT:
                    value = CommonUtil.leftPadValue(value, length, padding);
                    break;
                case CENTER:
                    value = CommonUtil.centerPadValue(value, length, padding);
                    break;
                default:
                    break;
            }
        }
        return this;
    }

    /**
     * Padding value by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return this
     */
    @Override
    public StringAssembler pad(FixedTypeInfo info) {
        return pad(info, Alignment.AUTO);
    }

    /**
     * Trim value by {@link FixedTypeInfo} and default alignment
     *
     * @param info             the {@link FixedTypeInfo}
     * @param defaultAlignment default alignment
     * @return this
     */
    @Override
    public StringAssembler trim(FixedTypeInfo info, Alignment defaultAlignment) {
        if (!info.getDefaultKeepPadding()) {
            Alignment alignment = getAlignment(info, defaultAlignment);
            Character padding = info.getDefaultPadding();

            switch (alignment) {
                case LEFT:
                    value = CommonUtil.trimRightBy(value, padding);
                    break;
                case RIGHT:
                    value = CommonUtil.trimLeftBy(value, padding);
                    break;
                case CENTER:
                    value = CommonUtil.trimBy(value, padding);
                    break;
                default:
                    break;
            }
        }
        return this;
    }

    /**
     * Trim value by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return this
     */
    @Override
    public StringAssembler trim(FixedTypeInfo info) {
        return trim(info, Alignment.AUTO);
    }

    /**
     * Get child {@link StringAssembler} by start position and length
     *
     * @param start  start position
     * @param length is size
     * @return child {@link StringAssembler}
     */
    @Override
    public StringAssembler child(Integer start, Integer length) {
        return new FixedStringAssembler(this.get(start, length));
    }

    /**
     * Get child {@link StringAssembler} by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return child {@link StringAssembler}
     */
    @Override
    public StringAssembler child(FixedTypeInfo info) {
        return child(info.getStart(), info.getLength());
    }

    /**
     * Check is blank value by blank padding character.
     *
     * @param blankPadding blank padding character.
     * @return is blank
     */
    @Override
    public boolean isBlank(Character blankPadding) {
        String value = this.value;
        if (blankPadding != null)
            value = CommonUtil.trimBy(this.value, blankPadding);
        return CommonUtil.isBlank(value);
    }

    /**
     * Check is blank value by {@link FixedTypeInfo}
     *
     * @param info the {@link FixedTypeInfo}
     * @return is blank
     */
    @Override
    public boolean isBlank(FixedTypeInfo info) {
        return isBlank(info.getDefaultNullPadding());
    }
}
