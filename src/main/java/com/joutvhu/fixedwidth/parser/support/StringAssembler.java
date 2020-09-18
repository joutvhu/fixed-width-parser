package com.joutvhu.fixedwidth.parser.support;

import com.joutvhu.fixedwidth.parser.domain.Alignment;
import com.joutvhu.fixedwidth.parser.domain.Padding;
import com.joutvhu.fixedwidth.parser.util.Assert;
import com.joutvhu.fixedwidth.parser.util.CommonUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Get or set a string value at a specified position.
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@Getter
public class StringAssembler {
    private String value;

    public StringAssembler(String value) {
        Assert.notNull(value, "String value can't be null.");
        this.value = value;
    }

    public static StringAssembler instance() {
        return new StringAssembler(StringUtils.EMPTY);
    }

    public static StringAssembler of(String value) {
        return new StringAssembler(value);
    }

    private static String padString(String value, Integer start, Integer length) {
        int size = length != null ? start + length : start;
        if (value.length() < size)
            value = StringUtils.rightPad(value, size);
        return value;
    }

    private void replaceAt(Integer start, Integer length, String value) {
        if (length == null)
            this.value = this.value.substring(0, start) + value;
        else
            this.value = this.value.substring(0, start) +
                    CommonUtil.rightPadValue(value, length, ' ') +
                    this.value.substring(start + length);
    }

    public int length() {
        return value != null ? value.length() : 0;
    }

    public String get(Integer start, Integer length) {
        if (start == null) start = 0;
        if (length == 0) length = null;
        this.value = padString(this.value, start, length);
        return length != null ? value.substring(start, start + length) : value.substring(start);
    }

    public String get(FixedTypeInfo info) {
        String value = get(0, info.getLength());
        Character padding = info.getPadding();
        Alignment alignment = info.getAlignment();
        if (padding != null && alignment != null) {
            if (padding == Padding.AUTO) padding = info.defaultPadding();

            switch (alignment) {
                case LEFT:
                    return CommonUtil.trimLeftBy(value, padding);
                case RIGHT:
                    return CommonUtil.trimRightBy(value, padding);
                case AUTO:
                case CENTER:
                    return CommonUtil.trimBy(value, padding);
                default:
                    return value;
            }
        }
        return value;
    }

    public StringAssembler set(Integer start, Integer length, String value) {
        if (start == null) start = 0;
        if (value == null) value = StringUtils.EMPTY;
        this.value = padString(this.value, start, length);
        this.replaceAt(start, length, value);
        return this;
    }

    public StringAssembler pad(FixedTypeInfo info) {
        return pad(info, Alignment.AUTO);
    }

    public StringAssembler pad(FixedTypeInfo info, Alignment defaultAlignment) {
        Integer length = info.getLength();
        if (length != null) {
            Alignment alignment = getAlignment(info, defaultAlignment);
            Character padding = info.defaultPadding();

            switch (alignment) {
                case LEFT:
                    value = CommonUtil.leftPadValue(value, length, padding);
                    break;
                case RIGHT:
                    value = CommonUtil.rightPadValue(value, length, padding);
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

    public StringAssembler black(FixedTypeInfo info) {
        if (info.getLength() != null && info.getLength() > 0)
            this.value = StringUtils.repeat(' ', info.getLength());
        return this;
    }

    private Alignment getAlignment(FixedTypeInfo info, Alignment defaultAlignment) {
        Alignment alignment = info.getAlignment();
        if (alignment == null || alignment == Alignment.AUTO) alignment = defaultAlignment;
        if (alignment == null || alignment == Alignment.AUTO) alignment = info.defaultAlignment();
        return alignment;
    }

    public StringAssembler child(Integer start, Integer length) {
        return new StringAssembler(this.get(start, length));
    }

    public StringAssembler child(FixedTypeInfo info) {
        return child(info.getStart(), info.getLength());
    }
}
