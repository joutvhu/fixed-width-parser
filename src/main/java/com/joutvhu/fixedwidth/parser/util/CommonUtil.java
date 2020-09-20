package com.joutvhu.fixedwidth.parser.util;

import com.google.re2j.Pattern;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

@UtilityClass
public class CommonUtil {
    /**
     * Check an object is null or empty
     *
     * @param value the object
     * @return the boolean
     */
    public boolean isBlank(Object value) {
        if (value == null) return true;
        if (value instanceof String)
            return ((String) value).isEmpty();
        if (value instanceof Collection)
            return ((Collection) value).isEmpty();
        if (value instanceof Map)
            return ((Map) value).isEmpty();
        if (value instanceof CharSequence)
            return ((CharSequence) value).length() == 0;
        if (value instanceof Object[])
            return ((Object[]) value).length == 0;
        return false;
    }

    /**
     * Check an object is not null and not empty
     *
     * @param value the object to be compared
     * @return the boolean
     */
    public boolean isNotBlank(Object value) {
        return !CommonUtil.isBlank(value);
    }

    public <T> T defaultIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public <T> List<T> listOf(T... value) {
        List<T> result = new ArrayList<>();
        Collections.addAll(result, value);
        return result;
    }

    public <T> Set<T> setOf(T... value) {
        Set<T> result = new HashSet<>();
        Collections.addAll(result, value);
        return result;
    }

    public <K, V> Map.Entry<K, V> mapEntryOf(K key, V value) {
        return new HashMap.SimpleEntry<>(key, value);
    }

    public static <K, V> Map<K, V> mapOfEntries(Map.Entry<? extends K, ? extends V>... entries) {
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<? extends K, ? extends V> e : entries)
            result.put(e.getKey(), e.getValue());
        return result;
    }

    /**
     * Left pad a String with a specified character.
     *
     * @param value the String to pad out, may be null
     * @param size  the size to pad to
     * @param pad   the character to pad with
     * @return left padded String, original String if no padding is necessary or cut String if the original String too long
     */
    public String leftPadValue(String value, int size, char pad) {
        int len = value.length();
        if (len == size) return value;
        else if (len < size) return StringUtils.leftPad(value, size, pad);
        else return StringUtils.substring(value, len - size, len);
    }

    /**
     * Right pad a String with a specified character.
     *
     * @param value the string need to be pad
     * @param size  the int need to be pad
     * @param pad   the char need to be cut
     * @return the value has right pad
     */
    public String rightPadValue(String value, int size, char pad) {
        int len = value.length();
        if (len == size) return value;
        else if (len < size) return StringUtils.rightPad(value, size, pad);
        else return StringUtils.substring(value, 0, size);
    }

    /**
     * Center pad a String with a specified character.
     *
     * @param value the string need to be pad
     * @param size  the int need to be pad
     * @param pad   the char need to be cut
     * @return the value has right pad
     */
    public String centerPadValue(String value, int size, char pad) {
        int len = value.length();
        if (len == size) return value;
        else if (len < size) {
            int padSize = size - len;
            int halfSize = padSize / 2;
            return StringUtils.repeat(pad, (padSize & 1) == 0 ? halfSize + 1 : halfSize) +
                    value + StringUtils.repeat(pad, halfSize);
        } else return StringUtils.substring(value, 0, size);
    }

    public String trimLeftBy(String value, char pad) {
        int i = 0;
        for (int len = value.length(); i < len; i++) {
            if (value.charAt(i) != pad)
                break;
        }
        return value.substring(i);
    }

    public String trimRightBy(String value, char pad) {
        int i = value.length() - 1;
        for (; i >= 0; i--) {
            if (value.charAt(i) != pad)
                break;
        }
        return value.substring(0, i + 1);
    }

    public String trimBy(String value, char pad) {
        return trimLeftBy(trimRightBy(value, pad), pad);
    }

    public boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i)))
                return true;
        }
        return false;
    }

    public String escapeRegular(String regex) {
        return Pattern.compile("([-/\\\\^$*+?.()|\\[\\]{}])").matcher(regex).replaceAll("\\\\$1");
    }

    public String replaceAll(String value, String search, Supplier<String> replacementSupplier) {
        if (value != null && search != null && value.contains(search)) {
            String replacement = replacementSupplier.get();
            if (replacement != null) {
                String regex = escapeRegular(search);
                return value.replaceAll(regex, replacement);
            }
        }
        return value;
    }

    public boolean isDateValid(String value, String datePattern, boolean strict) {
        if (value == null || datePattern == null || datePattern.length() <= 0)
            return false;

        SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        formatter.setLenient(false);

        try {
            formatter.parse(value);
        } catch (ParseException e) {
            return false;
        }

        return !strict || datePattern.length() == value.length();
    }
}
