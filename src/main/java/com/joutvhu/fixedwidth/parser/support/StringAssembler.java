package com.joutvhu.fixedwidth.parser.support;

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
            this.value = this.value.substring(0, start) + value + this.value.substring(start + length);
    }

    public String get(Integer start, Integer length) {
        if (start == null) start = 0;
        if (length == 0) length = null;
        this.value = padString(this.value, start, length);
        return length != null ? value.substring(start, start + length) : value.substring(start);
    }

    public String get(FixedTypeInfo info) {
        String value = get(0, info.getLength());
        if (info.getPadding() != null) {
            switch (info.getAlignment()) {
                case LEFT:
                    return CommonUtil.trimLeftBy(value, info.getPadding());
                case RIGHT:
                    return CommonUtil.trimRightBy(value, info.getPadding());
                case CENTRE:
                    return CommonUtil.trimBy(value, info.getPadding());
                default:
                    return value;
            }
        }
        return value;
    }

    public void set(String value, Integer start, Integer length) {
        if (start == null) start = 0;
        this.value = padString(this.value, start, length);
        this.replaceAt(start, length, value);
    }

    public StringAssembler child(Integer start, Integer length) {
        return new StringAssembler(this.get(start, length));
    }

    public StringAssembler child(FixedTypeInfo info) {
        return child(info.getStart(), info.getLength());
    }
}
