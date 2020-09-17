package com.joutvhu.fixedwidth.parser.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
public class ObjectUtil {
    public final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new SimpleModule())
            .registerModule(new JavaTimeModule());

    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return MAPPER.readValue(content, valueType);
        } catch (Exception e) {
            throw new FixedException(e);
        }
    }

    public Object parseDate(String value, Class<?> type, String format) {
        if (format != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            if (LocalDate.class == type)
                return LocalDate.parse(value, formatter);
            else if (java.sql.Date.class == type)
                return java.sql.Date.valueOf(LocalDate.parse(value, formatter));
            else if (java.sql.Time.class == type)
                return java.sql.Time.valueOf(LocalTime.parse(value, formatter));
            else if (java.sql.Timestamp.class == type)
                return java.sql.Timestamp.valueOf(LocalDateTime.parse(value, formatter));
            else if (LocalTime.class == type)
                return LocalTime.parse(value, formatter);
            else if (LocalDateTime.class == type)
                return LocalDateTime.parse(value, formatter);
            else if (Timestamp.class == type)
                return Timestamp.valueOf(LocalDateTime.parse(value, formatter));
            else if (Instant.class == type) {
                formatter = formatter.withZone(ZoneId.systemDefault());
                return Instant.from(formatter.parse(value));
            } else if (Date.class == type) {
                formatter = formatter.withZone(ZoneId.systemDefault());
                return Date.from(Instant.from(formatter.parse(value)));
            }
        }
        return readValue(value, type);
    }

    public Object parseNumber(String value, Class<?> type, String format) {
        BigDecimal number = null;
        if (CommonUtil.isBlank(format)) {
            DecimalFormat decimalFormat = new DecimalFormat(format);
            decimalFormat.setParseBigDecimal(true);
            try {
                number = (BigDecimal) decimalFormat.parse(value);
            } catch (ParseException e) {
                return readValue(value, type);
            }
        }

        if (byte.class.equals(type) || Byte.class.equals(type))
            return number != null ? number.byteValue() : Byte.valueOf(value);
        else if (short.class.equals(type) || Short.class.equals(type))
            return number != null ? number.shortValue() : Short.valueOf(value);
        else if (int.class.equals(type) || Integer.class.equals(type))
            return number != null ? number.intValue() : Integer.valueOf(value);
        else if (long.class.equals(type) || Long.class.equals(type))
            return number != null ? number.longValue() : Long.valueOf(value);
        else if (float.class.equals(type) || Float.class.equals(type))
            return number != null ? number.floatValue() : Float.valueOf(value);
        else if (double.class.equals(type) || Double.class.equals(type))
            return number != null ? number.doubleValue() : Double.valueOf(value);
        else if (AtomicInteger.class.equals(type))
            return new AtomicInteger(number != null ? number.intValue() : Integer.parseInt(value));
        else if (AtomicLong.class.equals(type))
            return new AtomicLong(number != null ? number.longValue() : Long.parseLong(value));
        else if (BigInteger.class.equals(type))
            return number != null ? number.toBigInteger() : new BigInteger(value);
        else if (BigDecimal.class.equals(type))
            return number != null ? number : new BigDecimal(value);
        return readValue(value, type);
    }
}
