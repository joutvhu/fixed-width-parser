package com.joutvhu.fixedwidth.parser.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@UtilityClass
public class TypeConstants {
    public final List<Class> STRING_TYPES = CommonUtil.listOf(String.class, Character.class, char.class);

    public final List<Class> BOOLEAN_TYPES = CommonUtil.listOf(Boolean.class, boolean.class);

    public final List<Class> DATE_TYPES = CommonUtil.listOf(Date.class, java.sql.Date.class, Timestamp.class, LocalDate.class, LocalTime.class, LocalDateTime.class, Instant.class);

    public final List<Class> INTEGER_NUMBER_TYPES = CommonUtil.listOf(Short.class, Integer.class, Long.class, BigInteger.class, short.class, int.class, long.class);

    public final List<Class> DECIMAL_NUMBER_TYPES = CommonUtil.listOf(Float.class, Double.class, BigDecimal.class, float.class, double.class);
}
