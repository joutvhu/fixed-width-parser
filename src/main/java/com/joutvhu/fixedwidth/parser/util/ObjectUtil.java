package com.joutvhu.fixedwidth.parser.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
@UtilityClass
public class ObjectUtil {
    public final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new SimpleModule())
            .registerModule(new JavaTimeModule());

    public String writeValue(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new FixedException(e);
        }
    }

    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return MAPPER.readValue(content, valueType);
        } catch (Exception e) {
            throw new FixedException(e);
        }
    }

    public String formatDate(Object value, String format) {
        if (CommonUtil.isNotBlank(format)) {
            Class<?> type = value.getClass();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            ZoneId zone = format.endsWith("Z") ? ZoneOffset.UTC : ZoneId.systemDefault();
            formatter = formatter.withZone(zone);

            if (LocalDate.class.equals(type))
                return formatter.format((LocalDate) value);
            else if (LocalTime.class.equals(type))
                return formatter.format((LocalTime) value);
            else if (LocalDateTime.class.equals(type))
                return formatter.format((LocalDateTime) value);
            else if (java.sql.Date.class.equals(type))
                return formatter.format(((java.sql.Date) value).toLocalDate());
            else if (java.sql.Time.class.equals(type))
                return formatter.format(((java.sql.Time) value).toLocalTime());
            else if (java.sql.Timestamp.class.equals(type))
                return formatter.format(((java.sql.Timestamp) value).toLocalDateTime());
            else if (Instant.class.equals(type))
                return formatter.format((Instant) value);
            else if (Date.class.equals(type))
                return formatter.format(((Date) value).toInstant());
        }
        return writeValue(value);
    }

    public Object parseDate(String value, Class<?> type, String format) {
        if (format != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            if (LocalDate.class.equals(type))
                return LocalDate.parse(value, formatter);
            else if (java.sql.Date.class.equals(type))
                return java.sql.Date.valueOf(LocalDate.parse(value, formatter));
            else if (java.sql.Time.class.equals(type))
                return java.sql.Time.valueOf(LocalTime.parse(value, formatter));
            else if (java.sql.Timestamp.class.equals(type))
                return java.sql.Timestamp.valueOf(LocalDateTime.parse(value, formatter));
            else if (LocalTime.class.equals(type))
                return LocalTime.parse(value, formatter);
            else if (LocalDateTime.class.equals(type))
                return LocalDateTime.parse(value, formatter);
            else if (Instant.class.equals(type)) {
                formatter = formatter.withZone(ZoneId.systemDefault());
                return Instant.from(formatter.parse(value));
            } else if (Date.class.equals(type)) {
                formatter = formatter.withZone(ZoneId.systemDefault());
                return Date.from(Instant.from(formatter.parse(value)));
            }
        }
        return readValue(value, type);
    }

    public Object parseNumber(String value, Class<?> type, String format) {
        BigDecimal number = null;
        if (CommonUtil.isNotBlank(format)) {
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
