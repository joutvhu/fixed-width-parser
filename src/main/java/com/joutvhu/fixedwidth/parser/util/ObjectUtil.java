package com.joutvhu.fixedwidth.parser.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joutvhu.fixedwidth.parser.exception.FixedException;
import lombok.experimental.UtilityClass;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

    public Object parseDate(String content, Class<?> valueType, String format) {
        if (format != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            if (LocalDate.class == valueType)
                return LocalDate.parse(content, formatter);
            else if (java.sql.Date.class == valueType)
                return java.sql.Date.valueOf(LocalDate.parse(content, formatter));
            else if (java.sql.Time.class == valueType)
                return java.sql.Time.valueOf(LocalTime.parse(content, formatter));
            else if (java.sql.Timestamp.class == valueType)
                return java.sql.Timestamp.valueOf(LocalDateTime.parse(content, formatter));
            else if (LocalTime.class == valueType)
                return LocalTime.parse(content, formatter);
            else if (LocalDateTime.class == valueType)
                return LocalDateTime.parse(content, formatter);
            else if (Timestamp.class == valueType)
                return Timestamp.valueOf(LocalDateTime.parse(content, formatter));
            else if (Instant.class == valueType) {
                formatter = formatter.withZone(ZoneId.systemDefault());
                return Instant.from(formatter.parse(content));
            } else if (Date.class == valueType) {
                formatter = formatter.withZone(ZoneId.systemDefault());
                return Date.from(Instant.from(formatter.parse(content)));
            }
        }
        return readValue(content, valueType);
    }
}
